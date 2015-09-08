(write #(. (document (firstFile "PsiManager.java")) (setText "Rollback!")))

(pool #(do (Thread/sleep 5000) (. IDE print "123")))

(action "TryMe!" "alt shift P" (fn [e] (let
  [e (event-data e 'PSI_ELEMENT)
      res (and (-> e nil? not) (.canFindUsages finds e)) ]
  (. IDE print (str "can find usages of '"  e "'? " (if res "Yes!" "Nope"))))))

(timer "bump" 500 #(. IDE (print (str (System/currentTimeMillis)  ": bump!"))))