(ns data-retrieval.ut.re-cluster-test
  (:require [clojure.test :refer :all]
            [data-retrieval.ut.re-cluster :refer :all]))

(def a
  [#{"colorado" "murder" "age" "voting" "killed" "18" "shooting" "nightclub" "zealand" "gay"}
   #{"jason" "dies" "rangers" "david"}
   #{"apple" "java" "kills" "crashes" "indonesia" "earthquake" "island" "dead"}
   #{"climate" "fund" "cop27"}
   #{"ukrainian" "nuclear" "ukraine" "shelling" "zaporizhzhia"}
   #{"beijing" "china" "death" "covid"}
   #{"teams" "armbands" "fifa"}
   #{"bob" "iger" "ceo" "disney"}
   #{"kurdish" "sing" "turkey" "istanbul" "iraq" "iran" "anthem" "syria" "bombing"}
   #{"twitter" "trump" "elon" "donald" "musk"}
   #{"kyiv" "winter" "worst"}
   #{"moon" "nasa"}
   #{"summit" "damage" "loss"}])

(def dict-a
  {:moon     11, :china 5, :summit 12, :kurdish 8, :elon 9, :kills 2, :bob 7, :murder 0,
   :disney   7, :worst 10, :anthem 8, :dies 1, :age 0, :dead 2, :rangers 1, :climate 3,
   :18       0, :donald 9, :nightclub 0, :ukraine 4, :twitter 9, :zealand 0, :ceo 7, :trump 9,
   :fund     3, :beijing 5, :ukrainian 4, :iran 8, :shooting 0, :iraq 8, :teams 6, :sing 8,
   :kyiv     10, :jason 1, :istanbul 8, :armbands 6, :turkey 8, :covid 5, :java 2, :damage 12,
   :iger     7, :island 2, :musk 9, :zaporizhzhia 4, :apple 2, :shelling 4, :gay 0,
   :colorado 0, :syria 8, :earthquake 2, :crashes 2, :winter 10, :voting 0, :death 5,
   :loss     12, :nasa 11, :bombing 8, :killed 0, :indonesia 2, :david 1, :fifa 6, :cop27 3,
   :nuclear  4})

(def b
  [#{"malaysia" "hung" "parliament"}
   #{"panda" "dies" "argentina"}
   #{"japan" "minister"}
   #{"colorado" "war" "missile" "daughter" "ukrainian" "jong" "warns" "killed" "funeral"
     "russia" "video" "rishi" "sunak" "kyiv" "18" "zelenskyy" "shooting" "nightclub"
     "nuclear" "ukraine" "gay" "shelling" "kim" "korea" "zaporizhzhia" "east"}
   #{"climate" "pakistan" "fund" "talks" "summit" "cop27" "damage" "loss"}
   #{"birthday" "father"}
   #{"sea" "harris" "island"}
   #{"china" "death" "covid"}
   #{"kurdish" "soldiers" "turkey" "istanbul" "launches" "airstrikes" "iraq" "syria" "bombing"}
   #{"twitter" "trump" "account" "elon" "reinstated" "donald" "musk"}
   #{"plane" "airport"}
   #{"qatar" "fifa"}
   #{"granddaughter" "biden"}
   #{"election" "kazakh"}])

(def c
  [#{"colorado" "war" "missile" "daughter" "ukrainian" "jong" "warns" "killed" "funeral"
     "russia" "video" "rishi" "sunak" "kyiv" "18" "zelenskyy" "shooting" "nightclub"
     "nuclear" "ukraine" "gay" "shelling" "kim" "korea" "zaporizhzhia" "east"}
   #{"panda" "dies" "argentina"}
   #{"sea" "harris" "island"}
   #{"japan" "minister"}
   #{"birthday" "father"}
   #{"china" "death" "covid"}
   #{"qatar" "fifa"}
   #{"plane" "airport"}
   #{"kurdish" "soldiers" "turkey" "istanbul" "launches" "airstrikes" "iraq" "syria" "bombing"}
   #{"twitter" "trump" "account" "elon" "reinstated" "donald" "musk"}
   #{"granddaughter" "biden"}
   #{"election" "kazakh"}
   #{"climate" "pakistan" "fund" "talks" "summit" "cop27" "damage" "loss"}
   #{"malaysia" "hung" "parliament"}])

(def d
  [#{}
   #{"jason" "dies" "rangers" "david"}
   #{"climate" "fund" "cop27"}
   #{"colorado" "murder" "age" "voting" "killed" "18" "shooting" "nightclub" "zealand" "gay"}
   #{"summit" "damage" "loss"}
   #{"ukrainian" "nuclear" "ukraine" "shelling" "zaporizhzhia"}
   #{"apple" "java" "kills" "crashes" "indonesia" "earthquake" "island" "dead"}
   #{"beijing" "china" "death" "covid"}
   #{"kurdish" "sing" "turkey" "istanbul" "iraq" "iran" "anthem" "syria" "bombing"}
   #{"twitter" "trump" "elon" "donald" "musk"}
   #{"bob" "iger" "ceo" "disney"}
   #{"teams" "armbands" "fifa"}
   #{"kyiv" "winter" "worst"}
   #{"moon" "nasa"}])

(def e
  [#{"colorado" "murder" "age" "voting" "killed" "18" "shooting" "nightclub" "zealand" "gay"}
   #{"jason" "dies" "rangers" "david"}
   #{"apple" "java" "kills" "crashes" "indonesia" "earthquake" "island" "dead"}
   #{}
   #{"climate" "fund" "cop27"}
   #{"beijing" "china" "death" "covid"}
   #{"teams" "armbands" "fifa"}
   #{"ukrainian" "nuclear" "ukraine" "shelling" "zaporizhzhia"}
   #{"kurdish" "sing" "turkey" "istanbul" "iraq" "iran" "anthem" "syria" "bombing"}
   #{"twitter" "trump" "elon" "donald" "musk"}
   #{"bob" "iger" "ceo" "disney"}
   #{"kyiv" "winter" "worst"}
   #{"summit" "damage" "loss"}
   #{"moon" "nasa"}])

(def dict-e
  {:moon      13, :china 5, :summit 12, :kurdish 8, :elon 9, :kills 2, :bob 10, :murder 0,
   :disney    10, :worst 11, :anthem 8, :dies 1, :age 0, :dead 2, :rangers 1, :climate 4,
   :18        0, :donald 9, :nightclub 0, :ukraine 7, :twitter 9, :zealand 0, :ceo 10,
   :trump     9, :fund 4, :beijing 5, :ukrainian 7, :iran 8, :shooting 0, :iraq 8,
   :teams     6, :sing 8, :kyiv 11, :jason 1, :istanbul 8, :armbands 6, :turkey 8,
   :covid     5, :java 2, :damage 12, :iger 10, :island 2, :musk 9, :zaporizhzhia 7,
   :apple     2, :shelling 7, :gay 0, :colorado 0, :syria 8, :earthquake 2, :crashes 2,
   :winter    11, :voting 0, :death 5, :loss 12, :nasa 13, :bombing 8, :killed 0,
   :indonesia 2, :david 1, :fifa 6, :cop27 4, :nuclear 7})

(deftest new-groups-test
  (is (= c (new-groups a b)))
  (is (= d (new-groups b a)))
  (is (= e (new-groups c a)))
  (is (= d (new-groups d e)))
  (is (= e (new-groups e d))))

(deftest str-sets->key-dict-test
  (is (= dict-a (str-sets->key-dict a)))
  (is (= dict-e (str-sets->key-dict e)))
  (is (= {:a 1, :b 1, :c 3}
         (str-sets->key-dict [#{} #{"a" "b"} #{} #{"c"} #{}]))))

(deftest key-dict->str-sets-test
  (is (= a (key-dict->str-sets dict-a)))
  (is (= e (key-dict->str-sets dict-e)))
  (is (= [#{} #{"a" "b"} #{} #{"c"}]
         (key-dict->str-sets {:a 1, :b 1, :c 3}))))
