# Deepthought

A novel graph-based reasoning engine that distributes neural network weights across a knowledge graph, enabling efficient training and inference without expensive GPU requirements.

## Overview

Deepthought challenges the conventional approach to language models by implementing a fundamentally different architecture: instead of dense parameter matrices requiring massive computational resources, it distributes model weights across edges in a graph database. This enables **localized learning** where only relevant connections update during training, dramatically reducing computational overhead.

### Core Innovation: Elastic Vectors

The system introduces "elastic vectors" - dynamic embeddings constructed on-demand from graph topology:

1. **Atomic Nodes**: Each node represents an atomic piece of information (currently words, extensible to any data type)
2. **Weighted Edges**: Connections between nodes carry the model's learned parameters
3. **Dynamic Vector Construction**: For any given node, an elastic vector is assembled by:
   - Gathering all edge weights from connected nodes
   - Aligning with a master atom index to ensure consistent dimensionality
   - Zero-padding for unconnected atoms
4. **Localized Updates**: Only edges connected to atoms present in a training example are updated

This architecture assumes that **global parameter updates are unnecessary** - a hypothesis counter to standard backpropagation but potentially more aligned with how knowledge naturally connects.

## Architecture

### Knowledge Graph Foundation

```
Node (Atom) → Edges (Weighted Connections) → Elastic Vector → Reasoning
```

- **Storage**: Neo4j or similar graph database
- **Nodes**: Atomic information units (words, concepts, entities)
- **Edges**: Weighted connections representing learned relationships
- **Vectors**: Dynamically constructed from node neighborhoods

### API v2: LLM-Competitive Interface

The Enhanced Reasoning Controller (`EnhancedReasoningController.java`) provides:

#### Endpoints

**`POST /api/v2/reason`** - Multi-step reasoning with explanations
- Transparent reasoning paths
- Confidence scoring
- Source attribution
- Alternative hypotheses

**`POST /api/v2/chat`** - Conversational interface
- Multi-turn context awareness
- Session management
- Optional reasoning visibility

**`POST /api/v2/reason/async`** - Complex query handling
- Non-blocking processing
- Polling-based result retrieval
- Extended reasoning steps

**`POST /api/v2/explain`** - Detailed explanation generation
- Reasoning decomposition
- Configurable explanation depth

**`POST /api/v2/knowledge/update`** - Dynamic knowledge integration
- Runtime graph updates without retraining
- Conflict resolution
- Source tracking

**`GET /api/v2/health`** - System capabilities
- Version info
- Feature enumeration
- Graph statistics

### Key Components

1. **GraphReasoningEngine**: Performs graph traversal and attention-based reasoning
2. **ConversationManager**: Maintains multi-turn context
3. **KnowledgeIntegrator**: Updates graph without full retraining
4. **ExplanationGenerator**: Produces human-readable reasoning chains

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

## Original Application: Web Mapping

Deepthought was initially developed for web application analysis:
- **Goal**: Predict which UI interactions cause state changes
- **Method**: Model page elements as nodes, interactions as edges
- **Outcome**: Improved mapping efficiency by focusing on high-probability transitions

This validated the elastic vector concept in a practical domain before extending to general reasoning.

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

## Usage Example

```bash
# Perform reasoning
curl -X POST http://localhost:8080/api/v2/reason \
  -H "Content-Type: application/json" \
  -d '{
    "query": "What causes economic recessions?",
    "context": ["historical patterns", "policy factors"],
    "maxSteps": 5,
    "sessionId": "session-123"
  }'

# Response includes:
# - conclusion: Synthesized answer
# - confidence: 0.0-1.0 score
# - explanation: Natural language reasoning
# - reasoningSteps: Path through graph
# - sources: Supporting nodes
# - alternativeHypotheses: Other possibilities
```

## Current Status & Roadmap

### Implemented
- ✅ Graph-based weight storage
- ✅ Elastic vector construction
- ✅ API v2 with modern interfaces
- ✅ Multi-turn conversation support
- ✅ Async reasoning for complex queries
- ✅ Dynamic knowledge updates

### In Progress
- 🔄 Graph attention optimization
- 🔄 Benchmarking against GPT/Claude
- 🔄 Multi-modal node support (images, audio)

### Planned
- ⬜ Formal localized learning proof
- ⬜ Distributed graph reasoning (multi-node databases)
- ⬜ Auto-scaling edge pruning
- ⬜ Reasoning cache optimization
- ⬜ Fine-grained explanation controls

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

This is an experimental architecture. Contributions should focus on:

1. **Benchmarking**: Compare against established baselines
2. **Theoretical Analysis**: Formalize localized learning properties
3. **Optimization**: Improve graph traversal efficiency
4. **Applications**: Test on diverse problem domains

## License

[Check repository for license details]

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