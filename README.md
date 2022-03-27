# clj-konmari

clj-konmari is a set of scripts to help with cleaning & organising of clojure
codebases.

The name is derived from [clj-kondo](https://github.com/clj-kondo/clj-kondo),
which in turn comes from the japanese author of the book "The Life-Changing
Magic of Tidying Up: The Japanese Art of Decluttering and Organizing" - *Marie
Kondo*

> Marie Kondo, also known as Konmari, is a Japanese organizing consultant, author, and TV show host. Kondo has written four books on organizing, which have collectively sold millions of copies around the world.

## Inconsistent aliases

This script will detect all namespace require forms which have inconsistent
aliases.

Eg: `one.clj` has a namespace require like `[common.utils :as utils]` and
`two.clj` has the same require but aliased like `[common.utils :as u]`

The script will detect this and print out, like so:

```
Running analysis on  src
common.utils                                    -> utils, u
```

[WIP] add an option for the script to ask user to select which alias to stick
to, and then output a clj-kondo compatible `:consistent-alias` map

#### Install

```bash
wget https://raw.githubusercontent.com/oxalorg/clj-konmari/main/src/konmari/inconsistent_aliases.clj 
```

#### Run

```bash
# make sure you have babashka installed
# src can be replaced by any directory
bb inconsistent_aliases.clj src
```

## More ideas

Have more ideas on how we can automate improving of clojure codebases? Create an issue please!
