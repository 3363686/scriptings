### IDE scripting
#### https://gist.github.com/gregsh/b7ef2e4ebbc4c4c11ee9

Here are my attempts to script an [IntelliJ-based IDE](https://youtrack.jetbrains.com/issue/IDEA-138252).

*IDE Scripting Console* is backed by [JSR-223](https://www.jcp.org/en/jsr/detail?id=223) (javax.script.*) API.

**Groovy**, **Clojure**, **JavaScript** and other scripting languages may be used.

Open *IDE Scripting Console*, type a statement, hit *Ctrl-Enter* to execute the current line or selection.

**.profile._language-extension_** file in the same directory will be executed along with it if present.

**CAUTION**: This is a real stuff! Try *System.exit(0)* once to get the idea that any damage is possible.
