# Data Retrieval

This project contains scripts to automate the daily retrieval and transformation of papercliff data.

## Functionality

The script at `src/data_retrieval/core.clj` is the entry point to the application and is responsible for executing the
daily tasks. It includes saving keywords, combinations, important nodes and edges, clusters, clustered graphs, and
actions.

## Important namespaces

1. `data_retrieval.tasks.collect`: This script is responsible for data collection. It calls the papercliff API to
retrieve data and save it in the form of keywords and combinations. The function `papercliff-data` uses the papercliff
and Github APIs to collect and save the data.

2. `data-retrieval.tasks.transform`: This script is responsible for transforming the collected data. It filters
important nodes and edges and saves the data accordingly. It also generates and saves graph data, clusters, clustered
graphs, and actions.

3. `data-retrieval.core`: This is the entry point to the application and is responsible for running the daily
tasks. It first checks if the actions file already exists. If not, it triggers the process to save keywords,
combinations, important nodes and edges, clusters, clustered graphs, and actions.

4. `data-retrieval.ut.re-cluster`: This script contains utility functions for re-clustering the collected data.
It provides methods for converting between string sets and key dictionaries, calculating the similarity between sets,
and generating new groups based on current and previous groupings.

## How to Run

To run the script, you will need a Clojure environment. Once that is set up, you can execute the script
`src/data_retrieval/core.clj`. This will start the daily tasks.

**Note:** Make sure to set the appropriate environment variables needed for your API calls.

## Dependencies

The project relies heavily on the [Loom](https://github.com/aysylu/loom) library for working with graph data structures.
