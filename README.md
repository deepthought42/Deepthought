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

### API v2: LLM-Style Interface ✅ IMPLEMENTED

The Enhanced Reasoning Controller (`EnhancedReasoningController.java`) provides LLM-style output capabilities through a hybrid architecture that combines graph-based reasoning with sequential text generation.

#### Endpoints

**`POST /api/v2/reason`** - Multi-step reasoning with explanations
```json
Request:
{
  "query": "What causes economic recessions?",
  "context": ["historical patterns", "policy factors"],
  "maxSteps": 5,
  "sessionId": "session-123",
  "config": {
    "temperature": 0.7,
    "maxTokens": 100,
    "maxHops": 3,
    "minConfidence": 0.1,
    "includeExplanation": true
  }
}

Response:
{
  "conclusion": "Economic recessions are typically caused by...",
  "confidence": 0.85,
  "explanation": "I reasoned through 3 paths in the knowledge graph...",
  "reasoningSteps": ["Starting from: economic", "Connected to 'policy'..."],
  "sources": ["economic", "policy", "interest-rates"],
  "sessionId": "session-123"
}
```

**`POST /api/v2/chat`** - Conversational interface
```json
Request:
{
  "message": "Tell me about machine learning",
  "sessionId": "session-123",
  "showReasoning": false,
  "config": {
    "temperature": 0.7,
    "maxTokens": 150
  }
}

Response:
{
  "message": "Machine learning is a branch of artificial intelligence...",
  "sessionId": "session-123",
  "confidence": 0.82,
  "sources": ["machine", "learning", "artificial", "intelligence"]
}
```

**`POST /api/v2/generate`** - Sequential text generation
```json
Request:
{
  "prompt": "The future of technology",
  "temperature": 0.8,
  "maxTokens": 100
}

Response:
{
  "generated_text": "The future of technology involves...",
  "prompt": "The future of technology"
}
```

**`POST /api/v2/explain`** - Detailed explanation generation
```json
Request:
{
  "query": "How does photosynthesis work?",
  "type": "STEP_BY_STEP"
}

Response:
{
  "explanation": "Reasoning Process:\n\nPath 1 (confidence: 0.850):\n  1. Starting from: photosynthesis\n  2. Connected to 'light' (weight: 0.85)...",
  "query": "How does photosynthesis work?",
  "paths_found": 3
}
```

**`POST /api/v2/knowledge/update`** - Dynamic knowledge integration
```json
Request:
{
  "source": "renewable",
  "target": "energy",
  "weight": 0.85,
  "source_id": "api",
  "conflict_strategy": "AVERAGE"
}

Response:
{
  "success": true,
  "source": "renewable",
  "target": "energy",
  "weight": 0.85
}
```

**`GET /api/v2/health`** - System capabilities
```json
Response:
{
  "status": "operational",
  "version": "2.0.0",
  "features": {
    "reasoning": true,
    "chat": true,
    "generation": true,
    "explanation": true,
    "knowledge_update": true,
    "multi_turn_conversation": true,
    "graph_reasoning": true
  },
  "statistics": {
    "total_features": 1250
  }
}
```

### Key Components

1. **GraphReasoningEngine**: Performs multi-hop graph traversal with attention-based feature relevance scoring
2. **TextGenerator**: Converts feature sequences into natural language using graph-based next-token prediction
3. **ConversationManager**: Maintains multi-turn context with session-based storage in Neo4j
4. **ExplanationGenerator**: Produces human-readable reasoning chains from graph paths
5. **KnowledgeIntegrator**: Updates graph at runtime with conflict resolution strategies

## Core Data Model

Deepthought's architecture is built on two fundamental objects that work together to enable graph-based learning:

### Feature Objects: Atomic Knowledge Units

`Feature` objects (`Feature.java`) are the building blocks of the knowledge graph. Each feature represents an observable attribute, concept, or token (e.g., "button", "click", "form", "submit").

**Key characteristics:**
- Stored as Neo4j `@NodeEntity` nodes in the graph database
- Identified by a string `value` (e.g., "login", "password")
- Connected to other features via weighted `@Relationship` edges
- Form a neural network-like structure where edges represent learned associations

**Graph structure:**
```java
// Features are nodes in the graph
Feature button = new Feature("button");
Feature click = new Feature("click");
Feature submit = new Feature("submit");

// FeatureWeight edges connect features with learned weights
// These weights are continuously updated through Q-learning
@RelationshipEntity(type = "HAS_RELATED_FEATURE")
class FeatureWeight {
    @StartNode Feature inputFeature;   // e.g., "button"
    @EndNode Feature outputFeature;     // e.g., "click"
    @Property double weight;            // learned association strength (0.0-1.0)
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
- Stored as Neo4j `@NodeEntity` nodes (like Features and Vocabularies)
- Capture complete prediction context: inputs, outputs, predicted feature, and actual outcome
- Store the policy matrix (weight snapshot) at the time of prediction
- Enable temporal learning: predict now, learn later when outcome is known
- Provide an immutable audit trail of the learning process

**What they store:**
```java
@NodeEntity
public class MemoryRecord {
    @Id @GeneratedValue 
    private Long id;
    
    @Property
    private Date date;                          // When prediction was made
    
    @Relationship(type = "DESIRED_FEATURE")
    private Feature desired_feature;            // What should have happened
    
    @Relationship(type = "PREDICTED")
    private Feature predicted_feature;          // What was predicted
    
    @Relationship(type = "PREDICTION", direction = OUTGOING)
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
├── main/java/com/
│   ├── deepthought/           # Core domain models
│   │   ├── models/           # Data models and entities
│   │   │   ├── edges/        # Edge types (FeaturePolicy, FeatureWeight, Prediction)
│   │   │   ├── Feature.java  # Feature node model (atomic knowledge units)
│   │   │   ├── Vocabulary.java # Vocabulary model (feature indexing)
│   │   │   └── MemoryRecord.java # Memory storage model
│   │   ├── repository/       # Data access layer
│   │   │   ├── FeatureRepository.java
│   │   │   ├── FeatureWeightRepository.java
│   │   │   ├── VocabularyRepository.java
│   │   │   ├── MemoryRecordRepository.java
│   │   │   ├── PredictionRepository.java
│   │   │   └── ConversationSessionRepository.java # ✅ NEW
│   │   └── services/         # Business logic
│   │       └── FeatureService.java
│   └── qanairy/              # Main application package
│       ├── api/              # REST controllers
│       │   ├── ReinforcementLearningController.java
│       │   └── v2/           # ✅ NEW API v2
│       │       ├── EnhancedReasoningController.java
│       │       └── dto/      # Data Transfer Objects
│       │           ├── ChatMessage.java
│       │           ├── ChatRequest.java
│       │           ├── ChatResponse.java
│       │           ├── GenerationConfig.java
│       │           ├── ReasoningRequest.java
│       │           └── ReasoningResponse.java
│       ├── brain/            # Core reasoning engine
│       │   ├── Brain.java    # Main reasoning orchestrator (✅ ENHANCED)
│       │   ├── GraphReasoningEngine.java # ✅ NEW
│       │   ├── TextGenerator.java # ✅ NEW
│       │   ├── ExplanationGenerator.java # ✅ NEW
│       │   ├── QLearn.java   # Reinforcement learning implementation
│       │   ├── FeatureVector.java # Elastic vector construction
│       │   ├── Predict.java  # Prediction algorithms
│       │   └── ActionFactory.java # Action creation utilities
│       ├── conversation/     # ✅ NEW - Conversation management
│       │   ├── ConversationManager.java
│       │   └── ConversationSession.java
│       ├── knowledge/        # ✅ NEW - Knowledge integration
│       │   └── KnowledgeIntegrator.java
│       ├── models/           # ✅ NEW - Additional models
│       │   └── ReasoningPath.java
│       ├── config/           # Configuration classes
│       │   ├── ConfigService.java
│       │   └── Neo4jConfiguration.java
│       ├── db/               # Database utilities
│       │   ├── DataDecomposer.java # JSON to graph conversion
│       │   └── VocabularyWeights.java # Vocabulary management
│       ├── deepthought/      # Application entry point
│       │   └── App.java      # Spring Boot main class
│       └── observableStructs/ # Observable data structures
│           ├── ConcurrentNode.java
│           ├── ObservableHash.java
│           └── ObservableQueue.java
├── test/java/                # Test classes
│   └── Qanairy/deepthought/
│       ├── BrainTests.java
│       ├── DataDecomposerTests.java
│       └── resourceClasses/
│           └── SelfContainedTestObject.java
└── resources/
    ├── application.properties # Configuration
    └── logback.xml           # Logging configuration
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

**Core Types:**
```java
// Node entity (stored in Neo4j)
@NodeEntity
public class Feature {
    private String value;  // e.g., "button", "click"
    private List<FeatureWeight> feature_weights;  // Outgoing edges
}

// Relationship entity (edges in Neo4j)
@RelationshipEntity(type = "HAS_RELATED_FEATURE")
public class FeatureWeight {
    @StartNode private Feature feature;      // Input feature
    @EndNode private Feature end_feature;    // Output feature
    @Property private double weight;         // Learned association (0.0-1.0)
}

// Index structure (node in Neo4j)
@NodeEntity
public class Vocabulary {
    private String label;              // Domain name
    private List<String> valueList;    // Ordered feature strings
    private Map<String, Integer> wordToIndexMap;  // Fast lookup
    
    // Thread-safe operations
    public synchronized int addWord(String word);
    public int getIndex(String word);
    public boolean[] createFeatureVector(List<String> words);
}

// Experience record (node in Neo4j)
@NodeEntity
public class MemoryRecord {
    private Date date;                          // Timestamp
    private Feature desired_feature;            // Actual outcome
    private Feature predicted_feature;          // What was predicted
    private List<Prediction> predictions;       // Full distribution
    private List<String> input_feature_values;  // Observed features
    private String[] output_feature_values;     // Possible outcomes
    private String policy_matrix_json;          // Weight snapshot
    
    // Store/retrieve policy matrix
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

### Configuration
Edit `src/main/resources/application.properties`:
```properties
# Neo4j Configuration
spring.data.neo4j.uri=bolt://localhost:7687
spring.data.neo4j.username=neo4j
spring.data.neo4j.password=password

# Server Configuration
server.port=8080

# Logging Configuration
logging.level.com.qanairy=DEBUG
logging.level.org.neo4j=WARN
```

### Running Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=BrainTests

# Run with coverage (requires jacoco plugin)
./mvnw test jacoco:report

# Run integration tests
./mvnw verify
```

## Prerequisites

### Required Software
- **Java**: JDK 8 or higher
- **Maven**: 3.6+ (or use included `mvnw`)
- **Neo4j**: 4.0+ (Community or Enterprise)
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
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   # Edit application.properties with your Neo4j credentials
   ```

4. **Build and Run**
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

5. **Test the API**
   ```bash
   curl -X POST http://localhost:8080/api/v2/health
   ```

## Installation

```bash
# Clone repository
git clone https://github.com/deepthought42/Deepthought.git
cd Deepthought

# Setup graph database (Neo4j recommended)
# Configure connection in application.properties

# Build
./mvnw clean install

# Run
./mvnw spring-boot:run
```

## Usage Examples

### Example 1: Multi-Step Reasoning

```bash
# Perform reasoning with explanations
curl -X POST http://localhost:8080/api/v2/reason \
  -H "Content-Type: application/json" \
  -d '{
    "query": "What causes economic recessions?",
    "context": ["historical patterns", "policy factors"],
    "maxSteps": 5,
    "config": {
      "temperature": 0.7,
      "maxTokens": 150,
      "maxHops": 3,
      "includeExplanation": true
    }
  }'

# Response includes:
# - conclusion: "Economic recessions are typically caused by..."
# - confidence: 0.85
# - explanation: "I reasoned through 3 paths in the knowledge graph..."
# - reasoningSteps: ["Starting from: economic", "Connected to 'policy'..."]
# - sources: ["economic", "policy", "interest-rates"]
```

### Example 2: Conversational Chat

```bash
# Start a conversation
curl -X POST http://localhost:8080/api/v2/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Tell me about machine learning",
    "config": {
      "temperature": 0.7,
      "maxTokens": 200
    }
  }'

# Response:
# {
#   "message": "Machine learning is a branch of artificial intelligence...",
#   "sessionId": "abc-123-def",
#   "confidence": 0.82,
#   "sources": ["machine", "learning", "artificial", "intelligence"]
# }

# Continue the conversation
curl -X POST http://localhost:8080/api/v2/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What are its practical applications?",
    "sessionId": "abc-123-def"
  }'
```

### Example 3: Text Generation

```bash
# Generate text from a prompt
curl -X POST http://localhost:8080/api/v2/generate \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "The future of renewable energy",
    "temperature": 0.8,
    "maxTokens": 100
  }'

# Response:
# {
#   "generated_text": "The future of renewable energy involves...",
#   "prompt": "The future of renewable energy"
# }
```

### Example 4: Knowledge Integration

```bash
# Add new knowledge to the graph
curl -X POST http://localhost:8080/api/v2/knowledge/update \
  -H "Content-Type: application/json" \
  -d '{
    "source": "renewable",
    "target": "sustainable",
    "weight": 0.90,
    "conflict_strategy": "AVERAGE"
  }'

# Response:
# {
#   "success": true,
#   "source": "renewable",
#   "target": "sustainable",
#   "weight": 0.90
# }
```

### Example 5: Using the Original RL API

```bash
# Make a prediction (existing API)
curl -X POST http://localhost:8080/rl/predict \
  -H "Content-Type: application/json" \
  -d 'input={"user":"test","action":"click"}&output_features=success,failure'

# Learn from feedback (existing API)
curl -X POST http://localhost:8080/rl/learn \
  -H "Content-Type: application/json" \
  -d 'memory_id=123&feature_value=success'
```

## Current Status & Roadmap

### ✅ Implemented (v2.0.0)
- ✅ Graph-based weight storage
- ✅ Elastic vector construction
- ✅ **API v2 with LLM-style interfaces**
- ✅ **Sequential text generation**
- ✅ **Multi-turn conversation support with session management**
- ✅ **Graph-based reasoning engine with multi-hop traversal**
- ✅ **Natural language explanation generation**
- ✅ **Dynamic knowledge updates with conflict resolution**
- ✅ **Conversational chat interface**
- ✅ **Temperature-based sampling for generation**
- ✅ **Attention-based feature relevance scoring**

### In Progress
- 🔄 Beam search optimization for generation
- 🔄 Vocabulary expansion and management
- 🔄 Benchmarking against GPT/Claude
- 🔄 Response caching for improved latency
- 🔄 Multi-modal node support (images, audio)

### Planned
- ⬜ Formal localized learning proof
- ⬜ Distributed graph reasoning (multi-node databases)
- ⬜ Auto-scaling edge pruning
- ⬜ Advanced attention mechanisms
- ⬜ Fine-tuning interface for domain adaptation
- ⬜ Streaming response generation
- ⬜ Advanced conflict resolution strategies

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
- Check Java version: `java -version` (should be 8+)

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
```

### Health Checks

Monitor system health:
```bash
# Check API health
curl http://localhost:8080/api/v2/health

# Check Neo4j health
curl http://localhost:7474/db/data/

# Check system resources
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