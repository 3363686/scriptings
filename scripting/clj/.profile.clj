(doseq [x (list (.project IDE) (.application IDE))]
  (let [pico (.getPicoContainer x)
       field (.getDeclaredField (.getClass pico) "componentKeyToAdapterCache")
       services (do (.setAccessible field true) (.get field pico))]
    (doseq [key (filter #(instance? String %) (.keySet services))]
       (let [match (re-find (re-matcher #"[\.]([^\.]+?)(Service|Manager|Helper|Factory)?$" key))
             groups (rest match)
             singular (empty? (rest groups))
             words (seq (com.intellij.psi.codeStyle.NameUtil/nameToWords (first groups)))
             short0 (clojure.string/join (flatten (list (.toLowerCase (first words)) (rest words))))
             shortName (if (false? singular) (com.intellij.openapi.util.text.StringUtil/pluralize short0) short0)]
         (try
           (intern *ns* (symbol shortName) (.getComponentInstance pico key))
         (catch Exception e ()))
       )
    )
  )
)

(defn file [^String x] (.findFileByUrl virtualFiles (com.intellij.openapi.vfs.VfsUtil/pathToUrl x)))
(defn file2 [^String x] (first (filter  #( = x (.getName %)) (.getOpenFiles fileEditors))))
(defn all-scope [] (com.intellij.psi.search.GlobalSearchScope/allScope (.project IDE)))
(defn findPsi [^String x] (com.intellij.psi.search.FilenameIndex/getFilesByName (.project IDE), x, (all-scope)))
(defn findFile [^String x] (com.intellij.psi.search.FilenameIndex/getVirtualFilesByName (.project IDE) x, (all-scope)) )
(defn firstPsi [^String x] (first (findPsi x)))
(defn firstFile [^String x] (first (findFile x)))
(defn ep [^String x] (.getExtensionPoint (com.intellij.openapi.extensions.Extensions/getArea nil) x))
(defn psi [x] (.findFile psis x))
(defn document [x] (.getDocument fileDocuments x))
(defn editor ([x] (.getEditor (.getSelectedEditor fileEditors x)))
             ([] (try (.. windows (getFocusedComponent (. IDE project)) (getEditor)) (catch Exception e ())  )) )

(defn #^Runnable runnable [x] (proxy [Runnable] [] (run [] (x))))
(defn write [x] (.. IDE application (runWriteAction (runnable x))))
(defn read [x] (.. IDE application (runReadAction (runnable x))))
(defn pool [x] (.. IDE application (executeOnPooledThread (runnable x))))
(defn swing [x] (com.intellij.util.ui.UIUtil/invokeLaterIfNeeded (runnable x)))

(defn dumpe [e] (.print IDE (str e (clojure.string/replace (str (seq (.getStackTrace e))) #"#<StackTraceElement " "\n  at "))))
(defn safe [x] (try (x) (catch Exception e (dumpe e))))
(defn data-key [e] (let [c ['com.intellij.openapi.actionSystem.LangDataKeys]]
  (some #(try (. (. (Class/forName (str %)) (getField (str e))) (get nil)) (catch Exception e nil) ) c)))
(defn event-data [e key] (.getData (data-key key) (.getDataContext e)))

(defn action [name & x]
  (.unregisterAction actions name)
  (.. keymaps getActiveKeymap (removeAllActionShortcuts name))

  (if (nil? (second x)) nil
    (let [[shortcut, perform] x]
       (. actions (registerAction name (proxy [com.intellij.openapi.actionSystem.AnAction] [name, name, nil]
          (actionPerformed [e] (perform e)) )))
       (if (= shortcut nil) nil
          (.. keymaps (getActiveKeymap) (addShortcut name
             (new com.intellij.openapi.actionSystem.KeyboardShortcut (javax.swing.KeyStroke/getKeyStroke shortcut) nil)) )
       )
       nil
    )
  )
)

(defn dispose [x] (let [t (if (instance? com.intellij.openapi.Disposable x) (x) (.put IDE x nil))]
  (if (nil? t) nil (com.intellij.openapi.util.Disposer/dispose t)) ) )

(defn timer [name & x] (do
  (dispose name)
  (if (nil? (second x)) nil
    (let [[delay, perform] x
      h (new com.intellij.util.Alarm (.project IDE))
      r (runnable (fn time_fun [] (do (perform) (. h (addRequest (runnable time_fun) delay)) ) ))]
      (. h (addRequest r delay))
      (. IDE (put name h))
    )
  ) ))
  