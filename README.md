# clj-konmari

clj-konmari is a set of scripts to help with cleaning & organising of clojure
codebases.

The name is derived from [clj-kondo](https://github.com/clj-kondo/clj-kondo),
which in turn comes from the japanese author of the book "The Life-Changing
Magic of Tidying Up: The Japanese Art of Decluttering and Organizing" - *Marie
Kondo*

> Marie Kondo, also known as Konmari, is a Japanese organizing consultant, author, and TV show host. Kondo has written four books on organizing, which have collectively sold millions of copies around the world.

## Youtube Video

I recorded my screen while building this, if you're curious check it out here: https://www.youtube.com/watch?v=bf8KLKkCH2g

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

> *Real world example*: https://github.com/metabase/metabase/issues/19930#issuecomment-1079935010

Optionally you can also choose preferred aliases for all of the above entries and output a `clj-kondo` compatible edn to `preferred_aliases.edn` file in the current directory (default not configurable atm). It will look like this:

```
➜  metabase git:(master) ✗ bb inconsistent_aliases.clj --choose src
Running analysis on  src
  1/85  metabase.automagic-dashboards.populate
    1: magic.populate
    2: populate
    3: [ignore]
    4: [custom]
Choose: 1
~~~
  2/85  metabase.models.setting.cache
    1: setting.cache
    2: cache
    3: [ignore]
    4: [custom]
Choose: 4
Enter a custom alias: cash
```

once all 85 preferred aliases are added, a file will be created, which is compatible with clj-kondo `[:consistent-alias :aliases]` config:

```
➜  metabase git:(master) ✗ cat preferred_aliases.edn
{metabase.automagic-dashboards.populate magic.populate, metabase.models.setting.cache cache, metabase.db.metadata-queries wowow, metabase.query-processor.util qputil}
```

#### Install

```bash
wget https://raw.githubusercontent.com/oxalorg/clj-konmari/main/src/konmari/inconsistent_aliases.clj 
```

#### Run

*Only print out inconsistent aliases*:

```bash
# make sure you have babashka installed
# src can be replaced by any directory
bb inconsistent_aliases.clj src
```

*Choose preferred aliases and output clj-kondo compatible edn*:

```bash
bb inconsistent_aliases.clj --choose src
```

## More ideas

Have more ideas on how we can automate improving of clojure codebases? Create an issue please!
