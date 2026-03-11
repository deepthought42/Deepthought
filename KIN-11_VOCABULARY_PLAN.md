# KIN-11 Implementation Plan: Learning Vocabulary

## Objective
Add automatic vocabulary learning so the system:
1. Detects strongly-related feature groups from feature-edge weights.
2. Computes similarity between vocabularies based on contained features.
3. Creates aggregate vocabularies for highly similar groups.
4. Attaches source vocabularies to the aggregate vocabulary node.

## Plan
- [x] Review current vocabulary/feature models, repositories, and learning flow.
- [x] Add repository support for linking child vocabularies to an aggregate vocabulary node.
- [x] Implement a `VocabularyService` that:
  - [x] Upserts a vocabulary from incoming training features.
  - [x] Clusters strong feature relationships (weight close to `1.0`, configurable threshold).
  - [x] Computes vocabulary similarity scores (Jaccard overlap of feature sets).
  - [x] Creates aggregate vocabularies when similarity threshold is met.
  - [x] Connects child vocabularies to created aggregate vocabulary nodes.
- [x] Integrate the service into the training endpoint so it runs during learning/training.
- [x] Add unit tests for clustering, similarity scoring, and aggregate vocabulary creation flow.
- [x] Run targeted tests and fix any regressions.

## Execution Notes
This plan has been fully implemented in this branch.
