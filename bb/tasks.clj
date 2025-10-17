(ns tasks
  (:require [babashka.fs :as fs]
            [babashka.process :as process]
            [cheshire.core :as json]))

;; Configuration
(defn read-addon-id
  "Read addon ID from addon.json"
  []
  (let [addon-json (json/parse-string (slurp "addon.json") true)]
    (:ankiweb_id addon-json)))

(def addon-config
  {:addon-dir (str (System/getProperty "user.home")
                   "/Library/Application Support/Anki2/addons21")
   :addon-id (read-addon-id)
   :source-path (System/getProperty "user.dir")})

(def addon-files
  ["__init__.py"
   "addon.json"
   "config.json"
   "config.md"])

;; Utilities
(defn exit-with-error
  "Print error message and exit"
  [& messages]
  (apply println messages)
  (System/exit 1))

(defn get-link-path
  "Get the full symlink path for the addon"
  []
  (str (:addon-dir addon-config) "/" (:addon-id addon-config)))

;; Tasks
(defn setup-symlink
  "Setup symlink for local addon development"
  []
  (let [{:keys [addon-dir source-path]} addon-config
        link-path (get-link-path)
        link (fs/file link-path)]
    (cond
      ;; Symlink exists - verify it points to the right location
      (fs/sym-link? link)
      (let [real-path (str (fs/read-link link))]
        (if (not= real-path source-path)
          (exit-with-error
           "ERROR: Symlink already exists but points to different location:"
           (str "  Current: " real-path)
           (str "  Expected: " source-path))
          (println "Symlink already exists and points to correct location")))

      ;; Path exists but not a symlink
      (fs/exists? link)
      (exit-with-error
       (str "ERROR: Path exists but is not a symlink: " link-path)
       "Please remove it manually before running this task")

      ;; Create new symlink
      :else
      (do
        (fs/create-dirs addon-dir)
        (fs/create-sym-link link-path source-path)
        (println "Symlink created successfully!")
        (println " " link-path "->" source-path)
        (println)
        (println "Restart Anki to load the addon")))))

(defn create-package
  "Create the addon package"
  []
  (let [release-dir "release"
        package-name "anki_restart.ankiaddon"
        package-path (str release-dir "/" package-name)]
    (fs/create-dirs release-dir)
    (apply process/shell "zip" "-j" package-path addon-files)
    (println (str "Package created: " package-path))))

;; dev
(comment
  ;; Test reading addon ID
  (read-addon-id)
  ;; => "1336227070"

  ;; Test addon config
  addon-config
  ;; => {:addon-dir "/Users/.../Library/Application Support/Anki2/addons21"
  ;;     :addon-id "1336227070"
  ;;     :source-path "/Users/.../anki-restart"}

  ;; Test link path
  (get-link-path)
  ;; => "/Users/.../Library/Application Support/Anki2/addons21/1336227070"

  ;; Test package creation
  (create-package)
  ;; => Creates release/anki_restart.ankiaddon

  ;; Test symlink setup
  (setup-symlink)
  ;; => Creates or verifies symlink
  )
