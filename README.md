# data-retrieval

This repository contains the scripts that extract data from the
[papercliff api](https://github.com/papercliff/api-documentation),
and apply transformations until they reach their final form.
Then, they can be used to create
[animated graphs](https://github.com/orgs/papercliff/repositories).

## Scripts

### Collect keywords

Extracts the most popular keywords for a running window of 24 hours 
that covers the last 7 days.

### Collect combinations

Extracts the most popular combinations/triples of keywords for a
running window of 24 hours that covers the last 7 days.

### Important nodes

Looks at the extracted keywords and finds the least popular keyword
for every instance of the running window. Then, it finds the most
popular of these (`infimum`), and with that, it filters out the
keywords that have less popularity. This process ensures that the
keywords are treated equally. The ones that were identified on a quiet
day have no advantage over the others.

### Important edges

The triples are turned into edges. Then the `infimum` of the edges is
calculated and used for filtering.

### Graph

The nodes and edges are combined to form the graph. The script makes
sure that:
* there is no isolated node
* the source and the target of every edge are important nodes
* the source of every edge is more important than the target

### Clusters

The nodes are divided into clusters (connected components).

### Clustered graph

A `cluster` is assigned to every node of the graph.

### Diffs

By looking at the successive instances of the running window, the script
highlights what nodes/edges are added/removed at each time.

### Actions

The results of the previous script (_diff_) become flat and an `action`
is attached to them:
* `"remove-edge"`
* `"remove-node"`
* `"add-node"`
* `"add-edge"`

### Loop

The last six scripts (_important edges_, _graph_, _clusters_,
_clustered graph_, _diffs_, _actions_) are executed again,
until the resulted actions have fewer edges.
So, the new actions will be easily visualized in a short video.

### Actions with days

New actions (`"change-day"`) are inserted.

## Usage

If you want to run the scripts, you will need to obtain an API key
from [rapidapi](https://rapidapi.com/mrdimosthenis/api/papercliff/)
and create the environment variable `X_RAPIDAPI_KEY`.

## Historical data

You can find the extracted and end-results files for the previous
weeks in the
[historical-data](https://github.com/papercliff/historical-data)
repository.
