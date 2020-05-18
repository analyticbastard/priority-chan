# priority-chan

A Clojure async channel with priorities and element removal

Elements can asynchronously be removed while stored
in the channel buffer.


## Usage

Create the channel

```clojure
(def rchan (chan))
(def pc (priority-chan 10        ; initial buffer size
                       :priority ; priority function predicate
                       :id       ; id function predicate
                       100       ; buffer removal interval
                       rchan))   ; removal channel
```

Add elements

```clojure
(>!! pc {:id 1 :priority 2})
(>!! pc {:id 2 :priority 4})
(>!! pc {:id 3 :priority 3})
(>!! pc {:id 4 :priority 1})
```

Delete elements

```clojure
(>!! rchan 2)
```

Read remaining elements in the specified piority order

```clojure
(<!! pc)
=> {:id 3, :priority 3}
(<!! pc)
=> {:id 1, :priority 2}
(<!! pc)
=> {:id 4, :priority 1}
```

## License

Copyright © 2020 Javier Arriero

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
