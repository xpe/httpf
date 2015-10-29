(ns httpf.graph
  (:require
   [httpf.utility :refer [map-group]]))

(defn system-nodes
  "Returns default system nodes."
  []
  (map-group
   :system
   {:B26 :start
    :B24 :is-service-available
    :B23 :is-uri-too-long
    :B22 :are-headers-too-large
    :B21 :is-method-implemented
    :B20 :are-content-headers-implemented
    :B19 :is-functionality-implemented
    :B18 :are-expect-extensions-implemented
    :B17 :is-system-block-ok
    :P26 :finish}))

(defn system-edges
  "Returns default system edges."
  []
  {[:B26 true]  :B24
   [:B26 false] :B24
   [:B24 true]  :B23
   [:B24 false] :handler/service-unavailable
   [:B23 true]  :handler/uri-too-long
   [:B23 false] :B22
   [:B22 true]  :handler/request-header-fields-too-large
   [:B22 false] :B21
   [:B21 true]  :B20
   [:B21 false] :handler/not-implemented
   [:B20 true]  :B19
   [:B20 false] :handler/not-implemented
   [:B19 true]  :B18
   [:B19 false] :handler/not-implemented
   [:B18 true]  :B17
   [:B18 false] :handler/expectation-failed
   [:B17 true]  :B12
   [:B17 false] :handler/internal-server-error})

(defn request-nodes
  "Returns default request nodes."
  []
  (map-group
   :request
   {:B12 :is-method-allowed
    :B11 :is-authorized
    :B10 :expects-continue
    :B09 :has-content
    :B08 :is-content-too-large
    :B07 :is-content-type-accepted
    :B06 :from-content
    :B05 :is-forbidden
    :B04 :is-method-trace
    :B03 :is-method-options
    :B02 :is-request-block-ok}))

(defn request-edges
  "Returns default request edges."
  []
  {[:B12 true]  :B11
   [:B12 false] :handler/method-not-allowed
   [:B11 true]  :B10
   [:B11 false] :handler/unauthorized
   [:B10 true]  :handler/continue
   [:B10 false] :B09
   [:B09 true]  :B08
   [:B09 false] :B05
   [:B08 true]  :handler/payload-too-large
   [:B08 false] :B07
   [:B07 true]  :B06
   [:B07 false] :handler/unsupported-media-type
   [:B06 true]  :B05
   [:B06 false] :handler/bad-request
   [:B05 true]  :handler/forbidden
   [:B05 false] :B04
   [:B04 true]  :handler/ok
   [:B04 false] :B03
   [:B03 true]  :handler/ok
   [:B03 false] :B02
   [:B02 true]  :C01
   [:B02 false] :handler/bad-request})

(defn accept-nodes
  "Returns default accept nodes."
  []
  (map-group
   :accept
   {:C01 :has-accept
    :C02 :accept-matches
    :D02 :has-accept-language
    :D03 :accept-language-matches
    :E03 :has-accept-charset
    :E04 :accept-charset-matches
    :F04 :has-accept-encoding
    :F05 :accept-encoding-matches
    :E06 :ignore-accept-block-mismatches}))

(defn accept-edges
  "Returns default accept edges."
  []
  {[:C01 true]  :C02
   [:C01 false] :D02
   [:C02 true]  :D02
   [:C02 false] :E06
   [:D02 true]  :D03
   [:D02 false] :E03
   [:D03 true]  :E03
   [:D03 false] :E06
   [:E03 true]  :E04
   [:E03 false] :F04
   [:E04 true]  :F04
   [:E04 false] :E06
   [:F04 true]  :F05
   [:F04 false] :G07
   [:F05 true]  :G07
   [:F05 false] :E06
   [:E06 true]  :G07
   [:E06 false] :handler/not-acceptable})

(defn retrieve-nodes
  "Returns default retrieve nodes."
  []
  (map-group
   :retrieve
   {:G07 :missing
    :I07 :moved
    :I06 :moved-permanently
    :I05 :moved-temporarily
    :I04 :gone-permanently}))

(defn retrieve-edges
  "Returns default retrieve edges."
  []
  {[:G07 true]  :H09
   [:G07 false] :C09
   [:I07 true]  :I06
   [:I07 false] :K03
   [:I06 true]  :handler/permanent-redirect
   [:I06 false] :I05
   [:I05 true]  :handler/temporary-redirect
   [:I05 false] :I04
   [:I04 true]  :handler/gone
   [:I04 false] :K03})

(defn precondition-nodes
  "Returns default precondition nodes."
  []
  (map-group
   :precondition
   {:C09 :has-if-match
    :C10 :has-if-unmodified-since
    :C11 :if-unmodified-since-matches
    :E09 :if-match-matches
    :E11 :has-if-none-match
    :E12 :has-if-modified-since
    :E13 :if-modified-since-matches
    :F11 :if-none-match-matches
    :F14 :is-precondition-safe
    :H09 :missing-has-precondition}))

(defn precondition-edges
  "Returns default precondition edges."
  []
  {[:C09 true]  :E09
   [:C09 false] :C10
   [:C10 true]  :C11
   [:C10 false] :E11
   [:C11 true]  :E11
   [:C11 false] :handler/precondition-failed
   [:E09 true]  :E11
   [:E09 false] :handler/precondition-failed
   [:E11 true]  :F11
   [:E11 false] :E12
   [:E12 true]  :E13
   [:E12 false] :J10
   [:E13 true]  :J10
   [:E13 false] :F14
   [:F11 true]  :J10
   [:F11 false] :F14
   [:F14 true]  :handler/not-modified
   [:F14 false] :handler/precondition-failed
   [:H09 true]  :handler/precondition-failed
   [:H09 false] :I07})

(defn create-nodes
  "Returns default create nodes."
  []
  (map-group
   :create
   {:K02 :is-method-create
    :K03 :create-is-method-put
    :L02 :create-path
    :L03 :create-partial-put
    :M02 :create
    :M03 :create-has-conflict}))

(defn create-edges
  "Returns default create edges."
  []
  {[:K02 true]  :L02
   [:K02 false] :handler/not-found
   [:K03 true]  :L03
   [:K03 false] :K02
   [:L02 true]  :M02
   [:L02 false] :handler/internal-server-error
   [:L03 true]  :handler/bad-request
   [:L03 false] :M03
   [:M02 true]  :N03
   [:M02 false] :handler/internal-server-error
   [:M03 true]  :handler/conflict
   [:M03 false] :N03})

(defn process-nodes
  "Returns default process nodes."
  []
  (map-group
   :process
   {:J10 :is-method-head-get
    :J11 :is-method-delete
    :J13 :is-method-put
    :J14 :is-method-process
    :K11 :process-delete
    :K13 :process-partial-put
    :K14 :process-has-conflict
    :L14 :process}))

(defn process-edges
  "Return default process edges."
  []
  {[:J10 true]  :N10
   [:J10 false] :J11
   [:J11 true]  :K11
   [:J11 false] :J13
   [:J13 true]  :K13
   [:J13 false] :J14
   [:J14 true]  :K14
   [:J14 false] :handler/internal-server-error
   [:K11 true]  :N14
   [:K11 false] :handler/internal-server-error
   [:K13 true]  :bad-request
   [:K13 false] :K14
   [:K14 true]  :handler/conflict
   [:K14 false] :L14
   [:L14 true]  :N14
   [:L14 false] :handler/internal-server-error})

(defn response-nodes
  "Returns default response nodes."
  []
  (map-group
   :response
   {:N03 :is-create-done
    :N04 :create-see-other
    :N08 :to-content
    :N09 :has-multiple-choices
    :N10 :see-other
    :N14 :is-process-done}))

(defn response-edges
  "Returns default response edges."
  []
  {[:N03 true]  :N04
   [:N03 false] :handler/accepted
   [:N04 true]  :handler/created
   [:N04 false] :handler/see-other
   [:N08 true]  :handler/ok
   [:N08 false] :handler/no-content
   [:N09 true]  :handler/multiple-choices
   [:N09 false] :N08
   [:N10 true]  :handler/see-other
   [:N10 false] :N09
   [:N14 true]  :N10
   [:N14 false] :handler/accepted})

(defn alternative-nodes
  "Returns default alternative nodes."
  []
  (map-group
   :alternative
   {:N22 :alternative-has-accept
    :N23 :alternative-accept-matches
    :N24 :alternative-to-content
    :O21 :is-response-alternative}))

(defn alternative-edges
  "Returns default alternative edges."
  []
  {[:N22 true]  :N23
   [:N22 false] :P25
   [:N23 true]  :N24
   [:N23 false] :P25
   [:N24 true]  :P25
   [:N24 false] :P25
   [:O21 true]  :N22
   [:O21 false] :P25
   [:P25 true]  :P26
   [:P25 false] :P26})

(defn nodes
  "Returns default nodes."
  []
  (merge (system-nodes)
         (request-nodes)
         (accept-nodes)
         (precondition-nodes)
         (retrieve-nodes)
         (create-nodes)
         (process-nodes)
         (response-nodes)
         (alternative-nodes)))

(defn edges
  "Returns default edges."
  []
  (merge (system-edges)
         (request-edges)
         (accept-edges)
         (precondition-edges)
         (retrieve-edges)
         (create-edges)
         (process-edges)
         (response-edges)
         (alternative-edges)))
