(defproject analyticbastard/priority-chan "0.1.0-SNAPSHOT"
  :description "A Clojure async channel with priorities"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "1.1.587"]]
  :main ^:skip-aot priority-chan.core-test
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/tools.nrepl "0.2.7"]]
                   :source-paths ["dev"]
                   :plugins [[cider/cider-nrepl "0.24.0"]]
                   :repl-options {:init-ns user
                                  :init (println "DFOM Engine REPL" *ns*)}}})