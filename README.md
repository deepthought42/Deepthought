# Deepthought

A novel graph-based reasoning engine that distributes neural network weights across a knowledge graph, enabling efficient training and inference without expensive GPU requirements.

## Overview

Deepthought challenges the conventional approach to language models by implementing a fundamentally different architecture: instead of dense parameter matrices requiring massive computational resources, it distributes model weights across edges in a graph database. This enables **localized learning** where only relevant connections update during training, dramatically reducing computational overhead.

### Core Innovation: Elastic Vectors

The system introduces "elastic vectors" - dynamic embeddings constructed on-demand from graph topology using `Feature` and `Vocabulary` objects:

1. **Feature Nodes**: Each `Feature` node represents an atomic piece of information (currently words/tokens, extensible to any data type)
2. **FeatureWeight Edges**: Connections between `Feature` nodes carry the model's learned parameters as edge weights
3. **Dynamic Vector Construction**: For any given feature, an elastic vector is assembled by:
   - Querying all `FeatureWeight` edges from connected features in Neo4j
   - Aligning with a `Vocabulary` index to ensure consistent dimensionality
   - Zero-padding for features not connected in the graph
4. **Localized Updates**: Only `FeatureWeight` edges connected to features present in a training example are updated via Q-learning

This architecture assumes that **global parameter updates are unnecessary** - a hypothesis counter to standard backpropagation but potentially more aligned with how knowledge naturally connects. By storing weights in the graph structure itself, the system achieves sparse, localized learning without dense parameter matrices.

### Tech Stack

- **Java 17**
- **Spring Boot 3.5** (with Spring Data Neo4j — driver-based, not OGM)
- **Neo4j 4.4+ or 5.x** (Bolt protocol)
- **Maven** (build); **JUnit 5** (tests, `@Tag("Regression")` for default suite)
- **SpringDoc OpenAPI 2.x** (Swagger UI at `/swagger-ui.html`)
- **Jakarta** namespaces (validation, annotations) for Boot 3

## Architecture

### Knowledge Graph Foundation

```
Feature (Node) → FeatureWeight (Edge) → Vocabulary (Index) → Elastic Vector → Reasoning
```

- **Storage**: Neo4j graph database
- **Nodes**: `Feature` objects - atomic information units (words, concepts, entities)
- **Edges**: `FeatureWeight` relationships - weighted connections representing learned associations
- **Indexing**: `Vocabulary` objects - maintain consistent feature-to-position mappings
- **Vectors**: Dynamically constructed from graph neighborhoods via vocabulary indexing
- **Learning**: Q-learning algorithm updates `FeatureWeight` values based on outcomes

### REST API

The application exposes two controller areas:

#### Reinforcement Learning (`/rl/*`)

`ReinforcementLearningController` provides prediction and learning endpoints:

| Method | Path | Description |
|--------|------|-------------|
| POST | `/rl/predict` | Make a prediction from JSON input and output feature labels; returns a `MemoryRecord` with the predicted feature and policy snapshot. |
| POST | `/rl/learn` | Apply learning feedback for a given memory ID and actual feature value; updates `FeatureWeight` edges via Q-learning. |
| POST | `/rl/train` | Run a training iteration with labeled JSON and a vocabulary label. |

Parameters (e.g. for `/rl/predict`): `input` (stringified JSON), `output_features` (comma-separated output labels).

#### Image Ingestion (`/images/*`)

`ImageIngestionController` provides image-to-graph ingestion:

| Method | Path | Description |
|--------|------|-------------|
| POST | `/images/ingest` | Accept a base64-encoded image; creates an original image node and derived nodes (outline, PCA, black-and-white, cropped objects) with `PART_OF` relationships to the original. |

Request body: `{ "image": "<base64 string>" }`.

#### API Documentation

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (when the app is running)
- **OpenAPI spec**: Generated during `mvn verify` (integration-test phase).

### Key Components

1. **Brain**: Orchestrates prediction and learning; builds policy matrices from the graph and applies Q-learning updates.
2. **DataDecomposer**: Converts JSON input into `Feature` lists for prediction.
3. **FeatureRepository / FeatureWeightRepository**: CRUD and custom Cypher for features and weighted edges.
4. **Neo4j**: Spring Boot 3 auto-configuration; Neo4j Java Driver and Spring Data Neo4j (no OGM).

## Core Data Model

Deepthought's architecture is built on two fundamental objects that work together to enable graph-based learning:

### Feature Objects: Atomic Knowledge Units

`Feature` objects (`Feature.java`) are the building blocks of the knowledge graph. Each feature represents an observable attribute, concept, or token (e.g., "button", "click", "form", "submit").

**Key characteristics:**
- Stored as Neo4j nodes using Spring Data Neo4j `@Node` (driver-based, not OGM)
- Identified by a string `value` (e.g., "login", "password")
- Connected to other features via `@Relationship(type = "HAS_RELATED_FEATURE")` to `FeatureWeight` relationship properties
- Form a neural network-like structure where edges represent learned associations

**Graph structure:**
```java
// Features are nodes in the graph
Feature button = new Feature("button");
Feature click = new Feature("click");
Feature submit = new Feature("submit");

// FeatureWeight is a relationship property type (HAS_RELATED_FEATURE)
// Weights are continuously updated through Q-learning
@RelationshipProperties
class FeatureWeight {
    @RelationshipId @GeneratedValue private Long id;
    @Property private double weight;           // learned association strength (0.0-1.0)
    @TargetNode private Feature end_feature;   // e.g., "click"
}
```

**In the learning process:**
- **Input features**: Current observations/state (what the system sees)
- **Output features**: Possible actions/outcomes (what the system can do)
- **FeatureWeight edges**: Learned probabilities connecting inputs to outputs

### Vocabulary Objects: Feature Space Indexing

`Vocabulary` objects (`Vocabulary.java`) provide the coordinate system for machine learning operations. They maintain consistent mappings between features and their positions in weight vectors.

**Key characteristics:**
- Ordered list of features defining a bounded domain (e.g., "web_elements", "ui_actions")
- Bidirectional mapping: word ↔ index for fast lookups
- Thread-safe operations for concurrent access
- Persisted in Neo4j alongside the feature graph

**Purpose:**
```java
// Create a vocabulary for a specific domain
Vocabulary webVocab = new Vocabulary("web_elements");

// Add features to define the feature space
webVocab.appendToVocabulary(new Feature("button"));
webVocab.appendToVocabulary(new Feature("input"));
webVocab.appendToVocabulary(new Feature("form"));

// Get consistent indexing
int buttonIndex = webVocab.getIndex("button");  // Returns: 0
int inputIndex = webVocab.getIndex("input");    // Returns: 1

// Create feature vectors for ML operations
List<String> observed = Arrays.asList("button", "form");
boolean[] stateVector = webVocab.createFeatureVector(observed);
// Result: [true, false, true] - button and form present, input absent
```

**How vocabularies enable elastic vectors:**
1. **Consistent dimensionality**: All vectors for a domain have the same size
2. **Sparse representation**: Only store weights for connected features in the graph
3. **Dynamic construction**: Build weight matrices on-demand from graph edges
4. **Zero-padding**: Unconnected features contribute zero to predictions

### How They Work Together

The relationship between Feature and Vocabulary objects implements the "elastic vector" concept:

```
┌─────────────────────────────────────────────────────────────┐
│                     LEARNING CYCLE                          │
└─────────────────────────────────────────────────────────────┘

1. Observations → Feature Objects
   [button, input, click] → Feature("button"), Feature("input"), Feature("click")

2. Features → Vocabulary Indexing
   Feature("button") → index 0, Feature("input") → index 1, Feature("click") → index 2

3. Vocabulary + Graph → Policy Matrix
   ┌─────────────────────────────────┐
   │        Output Features          │
   │      submit  cancel  validate   │
   ├─────────────────────────────────┤
   │ button │ 0.85   0.12    0.03   │  ← Query FeatureWeight edges
   │ input  │ 0.67   0.22    0.11   │     from graph database
   │ click  │ 0.92   0.05    0.03   │
   └─────────────────────────────────┘

4. Policy Matrix → Prediction
   Probability distribution over output features

5. Policy Matrix + Prediction → MemoryRecord
   Store prediction context: inputs, outputs, predicted feature, policy matrix
   Save MemoryRecord to Neo4j for future learning

6. Actual Outcome Known → Load MemoryRecord
   When feedback arrives (possibly later), retrieve the memory by ID

7. Compare Prediction to Outcome → Q-Learning Update
   Calculate rewards, update FeatureWeight edges in graph
   MemoryRecord remains unchanged (immutable audit trail)

8. Updated Graph → Improved Future Predictions
   Changed weights persist in Neo4j, next prediction uses new weights
```

**Key insight:** Features are the *data* (graph nodes with relationships), Vocabularies are the *indexing structure* that makes those features usable for matrix-based ML operations, and MemoryRecords are the *learning context* that enables temporal separation between prediction and feedback. Together, these three objects bridge the gap between graph databases and reinforcement learning algorithms.

### MemoryRecord Objects: Experience Replay & Learning Context

`MemoryRecord` objects (`MemoryRecord.java`) serve as **snapshots of prediction attempts** that enable retrospective learning from feedback. They're the bridge between making predictions and updating the knowledge graph.

**Key characteristics:**
- Stored as Neo4j nodes using Spring Data Neo4j `@Node` (like Features and Vocabularies)
- Capture complete prediction context: inputs, outputs, predicted feature, and actual outcome
- Store the policy matrix (weight snapshot) at the time of prediction
- Enable temporal learning: predict now, learn later when outcome is known
- Provide an immutable audit trail of the learning process

**What they store:**
```java
@Node
public class MemoryRecord {
    @Id @GeneratedValue 
    private Long id;
    
    @Property
    private Date date;                          // When prediction was made
    
    @Relationship(type = "DESIRED_FEATURE")
    private Feature desired_feature;            // What should have happened
    
    @Relationship(type = "PREDICTED")
    private Feature predicted_feature;          // What was predicted
    
    @Relationship(type = "PREDICTION", direction = Relationship.Direction.OUTGOING)
    private List<Prediction> predictions;       // Full probability distribution
    
    private List<String> input_feature_values; // Observed features (inputs)
    private String[] output_feature_values;    // Possible actions (outputs)
    
    private String policy_matrix_json;         // Serialized weight matrix snapshot
}
```

**The learning lifecycle:**

```
┌─────────────────────────────────────────────────────────────────┐
│                  MEMORY-BASED LEARNING CYCLE                    │
└─────────────────────────────────────────────────────────────────┘

Time T₀: PREDICTION PHASE
├─ Observe: ["button", "form", "input"]
├─ Query graph: Build policy matrix from FeatureWeight edges
├─ Predict: "click" (85% confidence)
└─ CREATE MemoryRecord:
    ├─ inputs: ["button", "form", "input"]
    ├─ outputs: ["click", "submit", "validate"]
    ├─ predicted: "click"
    ├─ policy_matrix: [[0.85, 0.12, 0.03], ...]  ← Snapshot
    └─ Save to Neo4j

Time T₁: ACTUAL OUTCOME KNOWN
└─ Actual outcome: "submit" (user clicked submit, not click)

Time T₂: LEARNING PHASE
├─ Load MemoryRecord by ID
├─ Compare: predicted="click", actual="submit"  ❌ Wrong!
├─ Calculate rewards for each input→output pair:
│   ├─ "button"→"submit": +1.0 (valid outcome, didn't predict)
│   ├─ "button"→"click": -1.0 (predicted but wrong)
│   └─ "form"→"submit": +1.0 (valid outcome)
├─ Q-Learning updates:
│   ├─ FeatureWeight("button"→"submit").weight += Δ  ↑ Strengthen
│   ├─ FeatureWeight("button"→"click").weight -= Δ   ↓ Weaken
│   └─ Save updated weights to graph
└─ MemoryRecord remains unchanged (historical record)

Time T₃: NEXT PREDICTION (same inputs)
├─ Query graph: NEW policy matrix with updated weights
├─ Predict: "submit" (62% confidence)  ✓ Learned!
└─ CREATE new MemoryRecord (learning continues)
```

**Memory vs. Feature Graph:**

| Aspect | Feature Graph | Memory Records |
|--------|--------------|----------------|
| **Purpose** | Current learned knowledge | Historical experience log |
| **Mutability** | Continuously updated | Immutable after creation |
| **Content** | Nodes (Features) + Edges (FeatureWeights) | Prediction attempts + outcomes |
| **Analogy** | Brain's current neural weights | Brain's training diary |
| **Query for** | "What does the system know NOW?" | "What did the system learn FROM?" |

**Relationship network in Neo4j:**
```
MemoryRecord ──[PREDICTED]──────> Feature("click")
    │
    ├──[DESIRED_FEATURE]────────> Feature("submit")
    │
    └──[PREDICTION]─────────────> Prediction(weight=0.85, feature="click")
                                   Prediction(weight=0.12, feature="submit")
                                   Prediction(weight=0.03, feature="validate")
```

The `Prediction` edges store the **full probability distribution**, enabling analysis of:
- Prediction confidence at decision time
- How close the system was to the correct answer
- Uncertainty/entropy metrics for model evaluation

**Why store policy matrices?**

The `policy_matrix_json` field preserves the exact weights used for prediction:
```java
// Compare historical weights to current weights
double[][] historicalPolicy = memory.getPolicyMatrix();
double[][] currentPolicy = brain.generatePolicy(sameInputs, sameOutputs);

// Analyze learning:
// - Weight deltas: How much changed after this experience?
// - Learning velocity: How quickly is the system adapting?
// - Convergence: Are weights stabilizing over time?
```

**Practical use cases:**

1. **Debugging Poor Predictions:**
```java
// Find high-confidence mistakes
List<MemoryRecord> overconfidentErrors = memories.stream()
    .filter(m -> !m.getPredictedFeature().equals(m.getDesiredFeature()))
    .filter(m -> getPredictionConfidence(m) > 0.8)
    .collect(Collectors.toList());

// Analyze: What input patterns cause overconfident errors?
```

2. **Tracking Learning Progress:**
```java
// Accuracy over time
long correctPredictions = memoryRepository
    .findByDateRange(startDate, endDate).stream()
    .filter(m -> m.getPredictedFeature().equals(m.getDesiredFeature()))
    .count();
```

3. **Experience Replay:**
```java
// Re-learn from historical experiences
List<MemoryRecord> oldMemories = memoryRepository.findByDateBefore(cutoff);
for (MemoryRecord memory : oldMemories) {
    brain.learn(memory.getID(), memory.getDesiredFeature());
}
// Reinforcement learning technique: learn multiple times from same experiences
```

**Thread safety & parallel learning:**
- Each `MemoryRecord` is independent
- Multiple predictions can create separate memories simultaneously
- Learning from one memory doesn't block others
- Neo4j transactions ensure consistency when updating shared FeatureWeights

**Key insight:** MemoryRecords are the system's "experience replay buffer" - they enable the temporal separation between prediction (Time T₀) and learning (Time T₁+), which is essential for reinforcement learning in real-world scenarios where feedback is delayed.

## Project Structure

```
src/
├── main/java/
│   ├── com/deepthought/        # Domain and API (deepthought namespace)
│   │   ├── models/             # Neo4j entities (Spring Data Neo4j @Node / @RelationshipProperties)
│   │   │   ├── edges/          # FeatureWeight, FeaturePolicy, Prediction, PartOf
│   │   │   ├── Feature.java
│   │   │   ├── Vocabulary.java
│   │   │   ├── MemoryRecord.java
│   │   │   └── ImageMatrixNode.java
│   │   ├── repository/         # Spring Data Neo4j repositories
│   │   │   ├── FeatureRepository.java
│   │   │   ├── FeatureWeightRepository.java
│   │   │   ├── VocabularyRepository.java
│   │   │   ├── MemoryRecordRepository.java
│   │   │   ├── PredictionRepository.java
│   │   │   ├── ImageMatrixNodeRepository.java
│   │   │   └── PartOfRepository.java
│   │   ├── services/
│   │   │   └── FeatureService.java
│   │   ├── api/
│   │   │   └── ReinforcementLearningController.java   # /rl/*
│   │   └── deepthought/
│   │       └── App.java       # Spring Boot main (package com.qanairy.deepthought)
│   └── com/qanairy/           # Application layer
│       ├── api/
│       │   └── ImageIngestionController.java          # /images/*
│       ├── brain/
│       │   ├── Brain.java
│       │   ├── QLearn.java
│       │   ├── FeatureVector.java
│       │   ├── Predict.java
│       │   └── ActionFactory.java
│       ├── config/
│       │   └── Neo4jConfiguration.java
│       ├── db/
│       │   ├── DataDecomposer.java
│       │   └── VocabularyWeights.java
│       ├── image/
│       │   └── ImageProcessingService.java
│       └── observableStructs/
├── test/java/
│   ├── com/deepthought/models/
│   │   └── VocabularyTests.java
│   ├── com/deepthought/models/edges/
│   │   └── PartOfTests.java
│   ├── com/deepthought/models/
│   │   └── ImageMatrixNodeTests.java
│   ├── com/qanairy/api/
│   │   ├── ReinforcementLearningControllerSmokeTests.java
│   │   ├── ImageIngestionControllerTests.java
│   │   └── dto/ImageIngestRequestTests.java
│   ├── com/qanairy/image/
│   │   └── ImageProcessingServiceTests.java
│   └── Qanairy/deepthought/
│       ├── BrainTests.java
│       ├── DataDecomposerTests.java
│       └── resourceClasses/SelfContainedTestObject.java
└── resources/
    └── application.properties
```

### Key Components Explained

**Domain Models:**
- **Feature.java**: Represents atomic knowledge units stored as Neo4j nodes with weighted relationships
- **Vocabulary.java**: Maintains feature-to-index mappings for consistent vector construction
- **FeatureWeight.java**: Relationship entity representing learned associations between features
- **MemoryRecord.java**: Stores prediction history and policy matrices for learning

**Core Engine:**
- **Brain.java**: Core reasoning engine that orchestrates prediction and learning cycles
- **QLearn.java**: Implements the Q-learning algorithm for updating FeatureWeight values
- **FeatureVector.java**: Constructs elastic vectors from graph topology on-demand
- **Predict.java**: Generates predictions from policy matrices

**Data & API:**
- **DataDecomposer.java**: Converts JSON input into Feature nodes and edges
- **ReinforcementLearningController.java**: Main REST API endpoint for the system
- **FeatureService.java**: Business logic for feature management and operations

**Repositories:**
- **FeatureRepository.java**: CRUD operations and queries for Feature nodes
- **VocabularyRepository.java**: Vocabulary management and similarity queries
- **FeatureWeightRepository.java**: Weight edge operations and updates

## Advantages Over Traditional LLMs

### Computational Efficiency
- **No GPU Required**: Graph operations are CPU-friendly
- **Selective Updates**: Only relevant connections train per example
- **Memory Efficient**: Sparse graph representation vs. dense matrices

### Transparency
- **Explainable Reasoning**: Graph paths show exact reasoning chains
- **Source Attribution**: Trace conclusions to specific knowledge nodes
- **Inspectable Weights**: Edge weights are individually queryable

### Dynamic Learning
- **Incremental Updates**: Add knowledge without full retraining
- **Conflict Resolution**: Explicitly handle contradictory information
- **Version Control**: Graph databases enable knowledge versioning

## Theoretical Foundation

### Localized Reinforcement Learning

The core hypothesis: **Neural networks over-parameterize**. For most inputs, only a small subset of parameters is relevant. By:

1. Identifying which atoms appear in training data
2. Finding their graph neighborhoods (connected atoms)
3. Updating only those edge weights

...we achieve comparable learning with drastically reduced computation.

### Graph Attention Mechanism

Rather than global attention (O(n²) for sequence length n), Deepthought uses:
- **Local attention**: Only examine connected nodes
- **Weighted propagation**: Edge weights modulate information flow
- **Multi-hop reasoning**: Traverse graph paths for inference

## Under the Hood: Implementation Details

This section explains how the core architecture concepts are actually implemented in code.

### From Observations to Predictions: Step-by-Step

**1. Feature Extraction**
```java
// Convert observations into Feature objects
List<Feature> inputFeatures = new ArrayList<>();
inputFeatures.add(new Feature("button"));
inputFeatures.add(new Feature("form"));
inputFeatures.add(new Feature("input"));

List<Feature> outputFeatures = new ArrayList<>();
outputFeatures.add(new Feature("click"));
outputFeatures.add(new Feature("submit"));
outputFeatures.add(new Feature("validate"));
```

**2. Vocabulary Alignment**
```java
// Create or load vocabulary for this domain
Vocabulary vocabulary = new Vocabulary("web_elements");

// Add features to vocabulary (assigns indices)
for (Feature f : inputFeatures) {
    vocabulary.appendToVocabulary(f);  // Maps feature to index position
}
for (Feature f : outputFeatures) {
    vocabulary.appendToVocabulary(f);
}

// Now each feature has a consistent index:
// "button" → 0, "form" → 1, "input" → 2, "click" → 3, etc.
```

**3. Policy Matrix Construction**
```java
// Brain.generatePolicy() queries the graph to build weight matrix
double[][] policy = new double[inputFeatures.size()][outputFeatures.size()];

for (int i = 0; i < inputFeatures.size(); i++) {
    for (int j = 0; j < outputFeatures.size(); j++) {
        // Query Neo4j for FeatureWeight edge between features
        List<Feature> connected = featureRepository.getConnectedFeatures(
            inputFeatures.get(i).getValue(),
            outputFeatures.get(j).getValue()
        );
        
        if (!connected.isEmpty()) {
            // Edge exists - use learned weight
            FeatureWeight edge = connected.get(0).getFeatureWeights().get(0);
            policy[i][j] = edge.getWeight();
        } else {
            // No edge yet - initialize random weight and create edge
            double weight = random.nextDouble();
            policy[i][j] = weight;
            featureRepository.createWeightedConnection(
                inputFeatures.get(i).getValue(),
                outputFeatures.get(j).getValue(),
                weight
            );
        }
    }
}

// Result: 3×3 matrix of weights from graph
// ┌─────────────────────┐
// │  click  submit valid│
// ├─────────────────────┤
// │ 0.73   0.15    0.12 │  button
// │ 0.54   0.21    0.25 │  form
// │ 0.41   0.38    0.21 │  input
// └─────────────────────┘
```

**4. Prediction Generation**
```java
// Brain.predict() sums columns and normalizes
double[] prediction = brain.predict(policy);

// Result: probability distribution over output features
// [0.56, 0.25, 0.19]  →  "click" most likely (56%)
```

**5. Memory Record Creation**
```java
// Store prediction for future learning
MemoryRecord memory = new MemoryRecord();
memory.setInputFeatureValues(inputFeatures.stream()
    .map(Feature::getValue).collect(Collectors.toList()));
memory.setOutputFeatureKeys(outputFeatures.stream()
    .map(Feature::getValue).toArray(String[]::new));
memory.setPolicyMatrix(policy);
memory.setPredictedFeature(outputFeatures.get(0)); // "click"

memoryRepository.save(memory);
```

**6. Learning from Feedback**
```java
// When actual outcome is known
Feature actualFeature = new Feature("submit");  // User actually clicked submit

// Brain.learn() updates weights via Q-learning
brain.learn(memory.getID(), actualFeature);

// Q-learning calculation:
// new_weight = old_weight + learning_rate × (reward + discount × estimated_future - old_weight)

// For each input feature:
for (String inputKey : memory.getInputFeatureValues()) {
    for (String outputKey : memory.getOutputFeatureKeys()) {
        // Calculate reward based on prediction accuracy
        double reward;
        if (outputKey.equals(actualFeature.getValue()) && 
            actualFeature.equals(memory.getPredictedFeature())) {
            reward = 2.0;  // Predicted correctly
        } else if (outputKey.equals(actualFeature.getValue())) {
            reward = 1.0;  // Didn't predict, but valid outcome
        } else if (outputKey.equals(memory.getPredictedFeature().getValue())) {
            reward = -1.0; // Predicted wrong
        } else {
            reward = -2.0; // Neither predicted nor actual
        }
        
        // Update FeatureWeight edge in graph
        FeatureWeight edge = getOrCreateEdge(inputKey, outputKey);
        double newWeight = qLearn.calculate(edge.getWeight(), reward, estimatedReward);
        edge.setWeight(newWeight);
        featureWeightRepository.save(edge);
    }
}

// Graph now contains updated weights for next prediction
```

### Why This Approach is Efficient

**Sparse Updates:**
- Only `FeatureWeight` edges involved in the prediction are queried
- Only edges related to the actual outcome are updated
- Unrelated parts of the graph remain untouched (no global backprop)

**On-Demand Construction:**
- Policy matrices are built only when needed for prediction
- Not stored as dense matrices - weights live in the graph
- Memory scales with number of relationships, not all possible combinations

**Incremental Learning:**
- New features can be added to vocabularies without retraining existing weights
- New edges are created organically as new feature combinations are observed
- The graph grows and adapts continuously

### Developer Quick Reference

**Core types (Spring Data Neo4j — driver-based, not OGM):**
```java
// Node (Neo4j)
@Node
public class Feature {
    @Id @GeneratedValue private Long id;
    private String value;  // e.g., "button", "click"
    @Relationship(type = "HAS_RELATED_FEATURE")
    private List<FeatureWeight> feature_weights;  // Outgoing edges
}

// Relationship properties (HAS_RELATED_FEATURE edge)
@RelationshipProperties
public class FeatureWeight {
    @RelationshipId @GeneratedValue private Long id;
    @Property private double weight;         // Learned association (0.0-1.0)
    @TargetNode private Feature end_feature;  // Target feature
}

// Index structure (node in Neo4j)
@Node
public class Vocabulary {
    @Id @GeneratedValue private Long id;
    private String label;              // Domain name
    private List<String> valueList;    // Ordered feature strings
    // wordToIndexMap is transient; rebuilt from valueList
    
    public synchronized int addWord(String word);
    public int getIndex(String word);
    public boolean[] createFeatureVector(List<String> words);
}

// Experience record (node in Neo4j)
@Node
public class MemoryRecord {
    @Id @GeneratedValue private Long id;
    private Date date;
    private Feature desired_feature;
    private Feature predicted_feature;
    @Relationship(type = "PREDICTION", direction = Relationship.Direction.OUTGOING)
    private List<Prediction> predictions;
    private List<String> input_feature_values;
    private String[] output_feature_values;
    private String policy_matrix_json;
    
    public void setPolicyMatrix(double[][] matrix);
    public double[][] getPolicyMatrix();
}
```

**Key Operations:**
```java
// Feature graph queries
featureRepository.findByValue("button");
featureRepository.getConnectedFeatures("button", "click");
featureRepository.createWeightedConnection("button", "click", 0.75);

// Vocabulary operations
vocabulary.appendToVocabulary(feature);
int index = vocabulary.getIndex("button");
boolean[] vector = vocabulary.createFeatureVector(observedFeatures);

// Prediction & memory creation
double[][] policy = brain.generatePolicy(inputFeatures, outputFeatures);
double[] prediction = brain.predict(policy);
MemoryRecord memory = new MemoryRecord();
memory.setInputFeatureValues(inputFeatureStrings);
memory.setOutputFeatureKeys(outputFeatureStrings);
memory.setPolicyMatrix(policy);
memory.setPredictedFeature(predictedFeature);
memoryRepository.save(memory);

// Learning from memory
brain.learn(memoryId, actualFeature);  // Updates FeatureWeight edges

// Memory analysis
Optional<MemoryRecord> memory = memoryRepository.findById(memoryId);
boolean correct = memory.get().getPredictedFeature()
                        .equals(memory.get().getDesiredFeature());
double[][] historicalWeights = memory.get().getPolicyMatrix();
```

**Threading & Concurrency:**
- `Vocabulary` uses `synchronized` methods and `AtomicInteger` for thread safety
- Multiple concurrent predictions can share the same vocabulary
- Each `MemoryRecord` is independent, enabling parallel prediction and learning
- Neo4j handles transactional consistency for graph updates
- Learning from one memory doesn't block learning from others
- Shared `FeatureWeight` edges are updated transactionally to prevent race conditions

## Original Application: Web Mapping

Deepthought was initially developed for web application analysis:
- **Goal**: Predict which UI interactions cause state changes
- **Method**: Model page elements as nodes, interactions as edges
- **Outcome**: Improved mapping efficiency by focusing on high-probability transitions

This validated the elastic vector concept in a practical domain before extending to general reasoning.

## Development Setup

### IDE Configuration

#### IntelliJ IDEA
1. Import as Maven project
2. Enable annotation processing
3. Configure Neo4j connection in Run Configuration
4. Install Neo4j plugin for database browsing

#### Eclipse
1. Import existing Maven project
2. Refresh Maven dependencies
3. Set project encoding to UTF-8
4. Install Spring Tools Suite (STS) for better Spring Boot support

#### VS Code
1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Install Neo4j extension for database management

### Database Setup

#### Local Neo4j Installation
```bash
# Download from https://neo4j.com/download/
# Start with default settings
# Default credentials: neo4j/neo4j
```

#### Docker Setup (Recommended)
```bash
# Basic setup
docker run -d \
  --name neo4j-deepthought \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/password \
  neo4j:4.4

# With APOC plugin for advanced operations
docker run -d \
  --name neo4j-deepthought \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/password \
  -e NEO4J_PLUGINS=["apoc"] \
  neo4j:4.4

# With persistent data storage
docker run -d \
  --name neo4j-deepthought \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/password \
  -v neo4j-data:/data \
  -v neo4j-logs:/logs \
  neo4j:4.4
```

#### Running with Docker

Build and run the full stack (app + Neo4j) with Docker Compose:

```bash
docker compose up --build
```

The app will be available at `http://localhost:8080` and Neo4j at `http://localhost:7474`. Neo4j credentials: `neo4j` / `password`.

To build only the app image (connect to an existing Neo4j):

```bash
docker build -t deepthought .
docker run -p 8080:8080 \
  -e SPRING_NEO4J_URI=bolt://host.docker.internal:7687 \
  -e SPRING_NEO4J_AUTHENTICATION_USERNAME=neo4j \
  -e SPRING_NEO4J_AUTHENTICATION_PASSWORD=password \
  deepthought
```

Use `host.docker.internal` when Neo4j runs on the host; with `docker compose`, the app typically uses hostname `neo4j` (see [Configuration](#configuration) for property names).

### Configuration

Edit `src/main/resources/application.properties`:

```properties
# Neo4j (Spring Boot 3 / Neo4j Java Driver)
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=password

# Server
server.port=8080

# Logging
logging.level.com.qanairy=DEBUG
logging.level.org.neo4j=WARN
```

### Running Tests

Tests use **JUnit 5 (Jupiter)**. Only tests tagged `@Tag("Regression")` run by default.

```bash
# Run Regression-tagged tests (default)
mvn test
# or
./mvnw test

# Run a specific test class
mvn test -Dtest=BrainTests

# Run all tests regardless of tag
mvn test -Djunit.jupiter.tags=

# Coverage report (JaCoCo)
mvn test jacoco:report

# Integration tests (includes OpenAPI generation)
mvn verify
```

**Test framework:** JUnit 5 (Jupiter). Tests that run with the default suite are tagged `@Tag("Regression")`. To run a different tag or all tests, use `-Djunit.jupiter.tags=YourTag` or `-Djunit.jupiter.tags=`.

**Writing new tests:** Use `org.junit.jupiter.api.Test`, `@BeforeEach` / `@BeforeAll`, and `org.junit.jupiter.api.Assertions` (e.g. `assertThrows` for expected exceptions). Tag regression tests with `@Tag("Regression")`.

## Prerequisites

### Required Software
- **Java**: JDK 17 or higher
- **Maven**: 3.6+ (or use included `./mvnw` if present)
- **Neo4j**: 4.4+ or 5.x (Bolt; compatible with Spring Data Neo4j driver)
- **Git**: For version control

### System Requirements
- **Memory**: Minimum 4GB RAM (8GB+ recommended)
- **Storage**: 2GB free space
- **Network**: Internet access for dependency downloads

### Development Tools (Recommended)
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **Database Browser**: Neo4j Browser or Desktop
- **API Testing**: Postman (collection included)

## Quick Start (5 minutes)

1. **Clone and Setup**
   ```bash
   git clone https://github.com/deepthought42/Deepthought.git
   cd Deepthought
   ```

2. **Start Neo4j**
   ```bash
   # Using Docker (recommended)
   docker run -p 7474:7474 -p 7687:7687 -e NEO4J_AUTH=neo4j/password neo4j:4.4
   
   # Or install locally and start service
   ```

3. **Configure Database**
   Edit `src/main/resources/application.properties`: uncomment and set `spring.neo4j.uri`, `spring.neo4j.authentication.username`, and `spring.neo4j.authentication.password` for your Neo4j instance.

4. **Build and Run**
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

5. **Test the API**
   ```bash
   # Swagger UI
   open http://localhost:8080/swagger-ui.html

   # Or call predict (example)
   curl -X POST "http://localhost:8080/rl/predict?input=%7B%22field%22%3A%22value%22%7D&output_features=LABEL_A,LABEL_B"
   ```

## Installation

```bash
# Clone repository
git clone https://github.com/deepthought42/Deepthought.git
cd Deepthought

# Configure Neo4j in src/main/resources/application.properties
# (spring.neo4j.uri, spring.neo4j.authentication.username/password)

# Build (requires JDK 17+)
mvn clean package
# or
./mvnw clean package

# Run
mvn spring-boot:run
# or
./mvnw spring-boot:run
```

## Usage Example

**Prediction and learning:**

```bash
# 1. Predict: send JSON input and output feature labels
curl -X POST "http://localhost:8080/rl/predict?input=%7B%22button%22%3Atrue%2C%22form%22%3Atrue%7D&output_features=click,submit,validate"

# Response: MemoryRecord (JSON) with id, predicted_feature, policy snapshot, etc.

# 2. Learn: send memory ID and actual outcome
curl -X POST "http://localhost:8080/rl/learn?memory_id=1&feature_value=submit"
```

**Image ingestion:**

```bash
curl -X POST http://localhost:8080/images/ingest \
  -H "Content-Type: application/json" \
  -d '{"image":"<base64-encoded-image-data>"}'
```

## Current Status & Roadmap

### Implemented
- ✅ Graph-based weight storage (Neo4j, Spring Data Neo4j driver)
- ✅ Elastic vector construction and Q-learning updates
- ✅ REST API: `/rl/predict`, `/rl/learn`, `/rl/train` and `/images/ingest`
- ✅ Image ingestion with derived nodes (outline, PCA, B&W, cropped objects) and `PART_OF` relationships
- ✅ OpenAPI/Swagger UI
- ✅ Java 17, Spring Boot 3.5, JUnit 5 test suite

### In Progress
- 🔄 Graph attention optimization
- 🔄 Multi-modal and image graph integration

### Planned
- ⬜ Formal localized learning proof
- ⬜ LLM-competitive API (e.g. reason/chat/explain endpoints)
- ⬜ Distributed graph reasoning
- ⬜ Auto-scaling edge pruning and reasoning cache optimization

## Critical Questions & Challenges

### Does Localized Learning Actually Work?

**Counter-argument**: Global optimization (standard backprop) works precisely because it adjusts ALL parameters, even those seemingly unrelated. Subtle parameter interactions might be lost with purely local updates.

**Response**: Empirical validation needed. Benchmark on standard NLP tasks (GLUE, SuperGLUE) comparing:
- Full backprop baseline
- Deepthought localized updates
- Hybrid approach (periodic global fine-tuning)

### Scalability of Graph Operations

**Counter-argument**: Graph traversal for every inference might be slower than matrix multiplication on GPUs, especially for deep reasoning (many hops).

**Response**: 
- Index optimization (spatial indexes for graph databases)
- Caching frequently accessed subgraphs
- Parallel traversal for independent reasoning paths
- Benchmark: inference latency vs. GPT-3.5 on standardized queries

### Knowledge Graph Completeness

**Counter-argument**: A sparse graph might lack connections needed for complex reasoning. Dense transformer attention catches subtle relationships that graphs might miss.

**Response**:
- Automatic edge creation from co-occurrence
- Configurable edge density thresholds
- "Elastic" means vectors adapt - add discovered connections dynamically
- Test on complex reasoning benchmarks (ARC, BIG-Bench Hard)

## Contributing

### Development Workflow

1. **Fork and Clone**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Deepthought.git
   cd Deepthought
   git remote add upstream https://github.com/deepthought42/Deepthought.git
   ```

2. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/issue-description
   ```

3. **Make Changes**
   - Follow existing code style
   - Add tests for new functionality
   - Update documentation as needed
   - Ensure all tests pass

4. **Test Your Changes**
   ```bash
   ./mvnw clean test
   ./mvnw spring-boot:run  # Manual testing
   ```

5. **Submit Pull Request**
   - Clear description of changes
   - Reference any related issues
   - Include test results
   - Update documentation if needed

### Code Standards

- **Java**: Follow Oracle Java Code Conventions
- **Naming**: Use descriptive names for classes and methods
- **Documentation**: Javadoc for public APIs
- **Testing**: Minimum 80% code coverage for new code
- **Commits**: Use conventional commit messages
  - `feat:` for new features
  - `fix:` for bug fixes
  - `docs:` for documentation changes
  - `test:` for test additions
  - `refactor:` for code refactoring

### Areas for Contribution

This is an experimental architecture. Contributions should focus on:

1. **Benchmarking**: Compare against established baselines (GPT, Claude, etc.)
2. **Theoretical Analysis**: Formalize localized learning properties
3. **Optimization**: Improve graph traversal efficiency
4. **Applications**: Test on diverse problem domains
5. **Testing**: Add comprehensive test coverage
6. **Documentation**: Improve API documentation and examples
7. **Performance**: Optimize memory usage and query performance
8. **Features**: Implement new reasoning capabilities

### Pull Request Guidelines

- **Title**: Clear, descriptive title
- **Description**: Explain what, why, and how
- **Tests**: Include test cases for new functionality
- **Documentation**: Update relevant documentation
- **Breaking Changes**: Clearly mark any breaking changes
- **Screenshots**: Include screenshots for UI changes

### Issue Reporting

When reporting issues, please include:
- **Environment**: OS, Java version, Neo4j version
- **Steps to Reproduce**: Clear, numbered steps
- **Expected Behavior**: What should happen
- **Actual Behavior**: What actually happens
- **Logs**: Relevant error logs or stack traces
- **Sample Data**: Minimal example that reproduces the issue

## Troubleshooting

### Common Issues

#### Neo4j Connection Failed
```
Error: Could not connect to Neo4j
```
**Solution**: 
- Verify Neo4j is running: `curl http://localhost:7474`
- Check credentials in `application.properties`
- Ensure firewall allows port 7687
- Test connection: `cypher-shell -u neo4j -p password`

#### Maven Build Fails
```
Error: Failed to resolve dependencies
```
**Solution**:
- Check internet connection
- Clear Maven cache: `./mvnw dependency:purge-local-repository`
- Update Maven: `./mvnw -U clean install`
- Check Java version: `java -version` (should be 17+)

#### Out of Memory Error
```
Error: Java heap space
```
**Solution**:
- Increase heap size: `export MAVEN_OPTS="-Xmx2g"`
- Or edit `pom.xml` to increase memory for tests
- Monitor memory usage during graph operations

#### Port Already in Use
```
Error: Port 8080 already in use
```
**Solution**:
- Change port in `application.properties`: `server.port=8081`
- Or kill existing process: `lsof -ti:8080 | xargs kill`
- Check what's using the port: `netstat -tulpn | grep 8080`

#### Graph Database Issues
```
Error: Transaction timeout
```
**Solution**:
- Increase transaction timeout in Neo4j configuration
- Optimize queries to reduce complexity
- Check for long-running transactions

#### Test Failures
```
Error: Tests failing with Neo4j connection
```
**Solution**:
- Ensure Neo4j is running before tests
- Check test database configuration
- Use embedded Neo4j for unit tests
- Verify test data setup

### Performance Issues

#### Slow Graph Queries
- **Index Optimization**: Create indexes on frequently queried properties
- **Query Optimization**: Use EXPLAIN to analyze query plans
- **Connection Pooling**: Configure Neo4j connection pool
- **Caching**: Implement query result caching

#### High Memory Usage
- **Graph Size**: Monitor graph size and prune unused nodes
- **Batch Operations**: Process large datasets in batches
- **Connection Management**: Close connections properly
- **Garbage Collection**: Tune JVM garbage collection

### Getting Help

- **Issues**: Create GitHub issue with full error logs
- **Discussions**: Use GitHub Discussions for questions
- **Documentation**: Check API_SPEC.md for detailed API info
- **Community**: Join discussions in GitHub Discussions
- **Email**: Contact maintainers for critical issues

### Debug Mode

Enable debug logging for troubleshooting:
```properties
# In application.properties
logging.level.com.qanairy=DEBUG
logging.level.org.neo4j=DEBUG
logging.level.org.springframework.data.neo4j=DEBUG
logging.level.com.deepthought=DEBUG
```

### Health Checks

Monitor system health:
```bash
# Swagger UI / API docs
open http://localhost:8080/swagger-ui.html

# Neo4j (if running)
curl http://localhost:7474

# Process
top -p $(pgrep java)
```

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Citation

If you use Deepthought in research:

```bibtex
@software{deepthought,
  author = {[Author Name]},
  title = {Deepthought: Graph-Based Reasoning Without GPUs},
  url = {https://github.com/deepthought42/Deepthought},
  year = {2025}
}
```

---

**Disclaimer**: This is an experimental system. Performance claims require rigorous validation against established benchmarks. The hypothesis that localized updates suffice for complex reasoning is unproven at scale.