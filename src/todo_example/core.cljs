(ns todo-example.core
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [reagent.react-native :as rn]
            [cljs.core.async :refer [go <!]]
            [cljs.core.async.interop :refer-macros [<p!]]
            ["@react-native-async-storage/async-storage" :as asyncStorage]))

;;alternative way to import
;(def asyncStorage (js/require "@react-native-async-storage/async-storage"))
(def async-storage (.-default asyncStorage))

(def styles {:main-panel       {:align-items "left" :padding 40}
             :task-complete    {:text-decoration-line "line-through"}
             :button-panel     {:flex-direction  "row"
                                :justify-content "space-between"
                                :margin-top      "10"
                                :width           "240"
                                :margin          "auto"}
             :button-container {:width 100}
             :task-border      {:border-color "gray" :border-width 1 :border-radius 5 :padding 5}})

(def initial-db {:tasks []})
(def db (r/atom {}))

(defn- mk-task
       ([value] (mk-task :not-started value))
       ([status value] {:status status :value value :editable? false}))

(defn- retrieve-item
       "Returns a channel with the value of k"
       [k]
       (go
         (-> (<p! (.getItem async-storage k))
             js/JSON.parse
             (js->clj :keywordize-keys true))))

(defn- save-item! [k v]
       (go (.setItem async-storage k (-> v clj->js js/JSON.stringify))
           v))

(defn- addable? [{:keys [value]}]
       (and (not (str/blank? value))
            (not (contains? (set (map :value (:tasks @db))) value))))

(defn- store-task!
       "Updates the entire db in local storage with the new task if it's present.
       Task is a map that should contain at least a value and a status key"
       [task]
       (when (addable? task)
             (println "must be addable")
             (let [new-db (swap! db update :tasks conj task)]
                  (save-item! "db" new-db))))

(defn- update-task!
       [idx {:keys [value] :as task}]
       (when (addable? task)
             (let [new-db (swap! db update-in [:tasks idx] assoc :value value :editable? false)]
                  (save-item! "db" new-db))))

(defn- clear-tasks! []
       (-> (.clear async-storage)
           (.then #(swap! db update :tasks (constantly [])))))

(defn- initialize-db!
       "Retrieves data from local storage and updates the app state.
       If local storage is empty, it is initialized"
       []
       (println "initializing db")
       (go
         (let [stored-db (or (<! (retrieve-item "db"))
                             (<! (save-item! "db" initial-db)))]
              (println "stored db is " stored-db)
              (reset! db stored-db))))

(defn- view-tasks [tasks]
       [rn/view
        (map-indexed
          (fn [idx {:keys [status value editable?] :as task}]
              (let [toggle-editable! (fn [editable?]
                                         (swap! db update-in [:tasks idx] assoc :editable? editable?))
                    task-value (r/atom value)
                    style (cond-> {:padding 0}
                                  (= :complete status) (merge (:task-complete styles))
                                  (:editable? task) (merge (:task-border styles)))]
                   ^{:key idx} [rn/touchable-without-feedback {:on-long-press #(toggle-editable! true)}
                                [rn/view
                                 [rn/text-input {:style               style :editable editable?
                                                 :multiline           true
                                                 :text-align-vertical "top"
                                                 :on-change-text      #(reset! task-value %)
                                                 :on-blur             #(toggle-editable! false)
                                                 :on-end-editing      #(update-task! idx {:value @task-value})} value]]])) tasks)])

(defn- menu []
       (let [task-val (r/atom nil)]
            [rn/view {:style (:main-panel styles)}
             [rn/text-input {:placeholder       "New Task"
                             :on-change-text    #(reset! task-val %)
                             :on-submit-editing #(store-task! (mk-task @task-val))}]
             [view-tasks (:tasks @db)]
             [rn/view {:style (:button-panel styles)}
              [rn/view {:style (:button-container styles)}
               [rn/button {:title "Remove" :color "#ad082b" :on-press initialize-db!}]]
              [rn/view {:style (:button-container styles)}
               [rn/button {:title "Add" :on-press #(store-task! (mk-task @task-val))}]]]]))

(defn app []
      [rn/view {:style {:flex 1 :align-items "center" :top 50}}
       [rn/text {:style {:font-size 50 :top 0}} "Task list"]
       [menu []]])

(defn ^:export -main [& args]
      (initialize-db!)
      (r/as-element [app]))

(comment
  (go
    (println (<p! (.getItem async-storage "tasks"))))
  (initialize-db!)

  (go (println (<p! (.getItem async-storage "db"))))
  @db

  (.clear async-storage)
  (store-task! {:status :complete :value "test123"})
  (save-item! "db" {:new [1 2 3]})
  (contains? #{"as"} "as")
  )