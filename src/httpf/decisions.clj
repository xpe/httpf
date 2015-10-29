(ns httpf.decisions
  (:require
   [httpf.utility :refer [log method= methods=]]
   [ring.util.response :refer [get-header]]))

;;; ===== System =====

(def ^{:group :system :diagram :B18} is-system-block-ok?
  "This is a final check in the 'system' category.

  By default, httpf returns true.

  Similarly to `is-request-block-ok?`, this decision can wrap any check
  that is missed by the HTTP Decision Diagram.

  Note from Andrei Neculau on Issue 41 of HTTPDD: 'This is in contrast
  say with how people use webmachine's callbacks: if a check is not
  done, then you would hijack the is_authorized and answer two questions
  at once. Now that the diagram has a programatic version (Cosmogol +
  JSON), I think extensibility should be covered by modifying the
  diagram and thus adjusting the flow to your needs. That said, this
  check doesn't hurt to be in there by default.'"
  (fn [ctx]
    true))

(def ^{:group :system :diagram :B19} are-expect-extensions-implemented?
  "Are the expect extensions implemented?

  By default, httpf returns true.

  'The 417 (Expectation Failed) status code indicates that the
  expectation given in the request's Expect header field (Section 5.1.1)
  could not be met by at least one of the inbound servers.'"
  (fn [ctx]
    true))

(def ^{:group :system :diagram :B20} is-functionality-implemented?
  "Is the requested functionality (other than methods and content headers)
  implemented?

  By default, httpf assumes the requested functionality is available.

  'The 501 (Not Implemented) status code indicates that the server does
  not support the functionality required to fulfill the request. This is
  the appropriate response when the server does not recognize the
  request method and is not capable of supporting it for any resource.'

  'A 501 response is cacheable unless otherwise indicated by the method
  definition or explicit cache controls (see Section 4.1.2
  of [Part6]).'"
  (fn [ctx]
    true))

(def ^{:group :system :diagram :B21} are-content-headers-implemented?
  "Are the content headers implemented?

  TODO.

  By default, httpf returns true."
  (fn [ctx]
    true))

(def ^{:group :system :diagram :B21} is-method-implemented?
  "Is the method implemented?

  By default, httpf assumes the GET, DELETE, HEAD, OPTIONS, PATCH, POST,
  PUT, and TRACE methods are implemented."
  (fn [ctx]
    (methods= ctx #{:delete :get :head :options :patch :post :put :trace})))

(def ^{:group :system :diagram :B22} are-headers-too-large?

  "Are the Content-* headers too large?

  By default, httpf assumes the Content-* headers are not too long."
  (fn [ctx]
    false))

(def ^{:group :system :diagram :B23} is-uri-too-long?
  "Is the URI too long?

  By default, httpf assumes the URI is not too long.

  'The 414 (URI Too Long) status code indicates that the server is refusing to
  service the request because the request-target (Section 5.3 of [Part1]) is
  longer than the server is willing to interpret. This rare condition is only
  likely to occur when a client has improperly converted a POST request to a GET
  request with long query information, when the client has descended into a
  'black hole' of redirection (e.g., a redirected URI prefix that points to a
  suffix of itself), or when the server is under attack by a client attempting
  to exploit potential security holes.'

  'A 414 response is cacheable unless otherwise indicated by the method
  definition or explicit cache controls (see Section 4.1.2 of [Part6]).'"
  (fn [ctx]
    false))

(def ^{:group :system :diagram :B24} is-service-available?
  "Is the service available to accept requests?

  By default, httpf assumes the service is available.

  'The 503 (Service Unavailable) status code indicates that the server is
  currently unable to handle the request due to a temporary overload or
  scheduled maintenance, which will likely be alleviated after some delay. The
  server MAY send a Retry-After header field (Section 7.1.3) to suggest an
  appropriate amount of time for the client to wait before retrying the
  request.'

  'Note: The existence of the 503 status code does not imply that a server has
  to use it when becoming overloaded. Some servers might simply refuse the
  connection.'"
  (fn [ctx]
    true))

(def ^{:group :system :diagram :B26} start?
  "This is a dummy function provided for symmetry. All nodes in httpf
  need to have a corresponding decision function."
  (fn [ctx]
    (log "> httpf.decisions/start?")
    true))

;;; ===== Request =====

(def ^{:group :request :diagram :B02} is-request-block-ok?
  "This is a final check in the 'request' category.

  By default, httpf returns true.

  Similarly to `is-system-block-ok?`, this decision can wrap any check
  that is missed by the HTTP Decision Diagram."
  (fn [ctx]
    true))

(def ^{:group :request :diagram :B03} is-method-options?
  "Is the request method equal to OPTIONS?"
  (fn [ctx]
    (method= ctx :options)))

(def ^{:group :request :diagram :B04} is-method-trace?
  "Is the request method equal to TRACE?"
  (fn [ctx]
    (method= ctx :trace)))

(def ^{:group :request :diagram :B05} is-forbidden?
  "Is the request forbidden?

  By default, httpf returns false.

  'The 403 (Forbidden) status code indicates that the server understood
  the request but refuses to authorize it. A server that wishes to make
  public why the request has been forbidden can describe that reason in
  the response payload (if any).'

  'If authentication credentials were provided in the request, the
  server considers them insufficient to grant access. The client SHOULD
  NOT automatically repeat the request with the same credentials. The
  client MAY repeat the request with new or different
  credentials. However, a request might be forbidden for reasons
  unrelated to the credentials.'

  'An origin server that wishes to 'hide' the current existence of a
  forbidden target resource MAY instead respond with a status code of
  404 (Not Found).'"
  (fn [ctx]
    false))

(def ^{:group :request :diagram :B06} from-content?
  "Does the request have content and did it parse correctly?

  By default, httpf returns true.

  This (and the associated flow handler) may be an important decision to
  customize, particular for PUT and POST requests. For example, for a
  request with Content-Type: application/json, the `from-content` flow
  handler would parse the JSON payload into an internal structure. The
  `from-content?` decision would return true if that structure is
  correct.

  If this decision returns false, the server will return 400 (Bad
  Request). Note that there are other decisions that can lead to the
  same result.

  'The 400 (Bad Request) status code indicates that the server cannot or
  will not process the request due to something that is perceived to be
  a client error (e.g., malformed request syntax, invalid request
  message framing, or deceptive request routing).'"
  (fn [ctx]
    true))

(def ^{:group :request :diagram :B07} is-content-type-accepted?
  "Is the content type accepted?

  By default, httpf returns true.

  'The 415 (Unsupported Media Type) status code indicates that the
  origin server is refusing to service the request because the payload
  is in a format not supported by this method on the target
  resource. The format problem might be due to the request's indicated
  Content-Type or Content-Encoding, or as a result of inspecting the
  data directly.'"
  (fn [ctx]
    true))

(def ^{:group :request :diagram :B08} is-content-too-large?
  "Is the request payload too large?

  By default, httpf returns false.

  'The 413 (Payload Too Large) status code indicates that the server is
  refusing to process a request because the request payload is larger
  than the server is willing or able to process. The server MAY close
  the connection to prevent the client from continuing the request.'

  'If the condition is temporary, the server SHOULD generate a
  Retry-After header field to indicate that it is temporary and after
  what time the client MAY try again.'"
  (fn [ctx]
    false))

(def ^{:group :request :diagram :B09} has-content?
  "Does the request have a payload; e.g. a content body?

  TODO: Test that this works for all four Ring types:
  1. String - The body is sent directly to the client.
  2. ISeq - Each element of the seq is sent to the client as a string.
  3. File - The contents of the referenced file is sent to the client.
  4. InputStream - The contents of the stream is sent to the client.
      When the stream is exhausted, the stream is closed."
  (fn [ctx]
    (get-in ctx [:request :body])))

(def ^{:group :request :diagram :B10} expects-continue?
  "Does the Expect header field equal '100-continue'? Both the header
  field and the value are compared case insensitively.

  The 'Expect' header field in a request indicates a certain set of
  behaviors (expectations) that need to be supported by the server in
  order to properly handle this request. The only such expectation
  defined by this specification is 100-continue.'"
  (fn [ctx]
    (some->> (get-header (:request ctx) "expect")
             (re-find #"(?i)^100-continue$"))))

(def ^{:group :request :diagram :B11} is-authorized?
  "Is the request authorized; e.g. if necessary, does it have valid
  authentication credentials?

  By default, httpf assumes the request is authorized.

  'The 401 (Unauthorized) status code indicates that the request has not
  been applied because it lacks valid authentication credentials for the
  target resource. The origin server MUST send a WWW-Authenticate header
  field (Section 4.4) containing at least one challenge applicable to
  the target resource. If the request included authentication
  credentials, then the 401 response indicates that authorization has
  been refused for those credentials. The client MAY repeat the request
  with a new or replaced Authorization header field (Section 4.1). If
  the 401 response contains the same challenge as the prior response,
  and the user agent has already attempted authentication at least once,
  then the user agent SHOULD present the enclosed representation to the
  user, since it usually contains relevant diagnostic information.'"
  (fn [ctx]
    true))

(def ^{:group :request :diagram :B12} is-method-allowed?
  "Is the request method allowed?

  This is probably one of the first decisions you will need to
  customize.

  By default, httpf allows the GET and HEAD methods.

  From http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html - The
  return code of the response always notifies the client whether a
  method is currently allowed on a resource, since the set of allowed
  methods can change dynamically. An origin server SHOULD return the
  status code 405 (Method Not Allowed) if the method is known by the
  origin server but not allowed for the requested resource, and 501 (Not
  Implemented) if the method is unrecognized or not implemented by the
  origin server. The methods GET and HEAD MUST be supported by all
  general-purpose servers. All other methods are OPTIONAL; however, if
  the above methods are implemented, they MUST be implemented with the
  same semantics as those specified in section 9."
  (fn [ctx]
    (methods= ctx #{:head :get})))

;;; ===== Accept =====

(def ^{:group :accept :diagram :C01} has-accept?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :accept :diagram :C02} accept-matches?
  "Does the Accept request header match the provided Content-Type header?

  By default, httpf returns true."
  (fn [ctx]
    true))

(def ^{:group :accept :diagram :D02} has-accept-language?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :accept :diagram :D03} accept-language-matches?
  "TODO"
  (fn [ctx]
    true))

(def ^{:group :accept :diagram :E03} has-accept-charset?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :accept :diagram :E04} accept-charset-matches?
  "TODO"
  (fn [ctx]
    true))

(def ^{:group :accept :diagram :E06} ignore-accept-block-mismatches?
  "Ignore accept block mismatches?

  TODO"
  (fn [ctx]
    true))

(def ^{:group :accept :diagram :F04} has-accept-encoding?
  "Does the request have an Accept-Encoding header?

  'The 'Accept-Encoding' header field can be used by user agents to
  indicate what response content-codings (Section 3.1.2.1) are
  acceptable in the response. An 'identity' token is used as a synonym
  for 'no encoding' in order to communicate when no encoding is
  preferred.'

      Accept-Encoding  = #( codings [ weight ] )
      codings          = content-coding / 'identity' / '*'

  'Each codings value MAY be given an associated quality value
  representing the preference for that encoding, as defined in Section
  5.3.1. The asterisk '*' symbol in an Accept-Encoding field matches any
  available content-coding not explicitly listed in the header field.'

  'For example,

      Accept-Encoding: compress, gzip
      Accept-Encoding:
      Accept-Encoding: *
      Accept-Encoding: compress;q=0.5, gzip;q=1.0
      Accept-Encoding: gzip;q=1.0, identity; q=0.5, *;q=0

  'A request without an Accept-Encoding header field implies that the
  user agent has no preferences regarding content-codings.  Although
  this allows the server to use any content-coding in a response, it
  does not imply that the user agent will be able to correctly process
  all encodings.'

  'A server tests whether a content-coding for a given representation is
  acceptable using these rules:'

  '1. If no Accept-Encoding field is in the request, any content-coding
  is considered acceptable by the user agent.'

  '2. If the representation has no content-coding, then it is acceptable
  by default unless specifically excluded by the Accept-Encoding field
  stating either 'identity;q=0' or '*;q=0' without a more specific entry
  for 'identity'.'

  '3. If the representation's content-coding is one of the
  content-codings listed in the Accept-Encoding field, then it is
  acceptable unless it is accompanied by a qvalue of 0.  (As defined in
  Section 5.3.1, a qvalue of 0 means 'not acceptable'.)'

  '4. If multiple content-codings are acceptable, then the acceptable
  content-coding with the highest non-zero qvalue is preferred.'

  'An Accept-Encoding header field with a combined field-value that is
  empty implies that the user agent does not want any content-coding in
  response.  If an Accept-Encoding header field is present in a request
  and none of the available representations for the response have a
  content-coding that is listed as acceptable, the origin server SHOULD
  send a response without any content-coding.'

 'Note: Most HTTP/1.0 applications do not recognize or obey qvalues
  associated with content-codings.  This means that qvalues might not
  work and are not permitted with x-gzip or x-compress.'"
  (fn [ctx]
    false))

(def ^{:group :accept :diagram :F05} accept-encoding-matches?
  "Accept encoding matches?

  TODO"
  (fn [ctx]
    true))

;;; ===== Retrieve =====

(def ^{:group :retrieve :diagram :I04} gone-permanently?
  "Is the resource permanently gone?

  By default, httpf returns false.

  This may be an important decision to customize."
  (fn [ctx]
    false))

(def ^{:group :retrieve :diagram :I05} moved-temporarily?
  "Has the resource been moved temporarily?

  By default, httpf returns false.

  This may be an important decision to customize."
  (fn [ctx]
    false))

(def ^{:group :retrieve :diagram :I06} moved-permanently?
  "Has the resource been moved permanently?

  By default, httpf returns false.

  This may be an important decision to customize."
  (fn [ctx]
    false))

(def ^{:group :retrieve :diagram :I07} moved?
  "Has the resource moved?

  By default, httpf returns false.

  This may be an important decision to customize."
  (fn [ctx]
    false))

(def ^{:group :retrieve :diagram :G07} missing?
  "Is the resource missing?

  By default, httpf returns false.

  This may be an important decision to customize."
  (fn [ctx]
    false))

;; precondition

;;; ===== Precondition =====

(def ^{:group :precondition :diagram :C09} has-if-match?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :precondition :diagram :C10} has-if-unmodified-since?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :precondition :diagram :C11} if-unmodified-since-matches?
  "TODO"
  (fn [ctx]
    true))

(def ^{:group :precondition :diagram :E11} has-if-none-match?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :precondition :diagram :E12} has-if-modified-since?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :precondition :diagram :E09} if-match-matches?
  "TODO"
  (fn [ctx]
    true))

(def ^{:group :precondition :diagram :E13} if-modified-since-matches?
  "TODO"
  (fn [ctx]
    true))

(def ^{:group :precondition :diagram :F11} if-none-match-matches?
  "TODO"
  (fn [ctx]
    true))

(def ^{:group :precondition :diagram :F14} is-precondition-safe?
  "TODO"
  (fn [ctx]
    true))

(def ^{:group :precondition :diagram :H09} missing-has-precondition?
  "TODO"
  (fn [ctx]
    false))

;;; ===== Create =====

(def ^{:group :create :diagram :K02} is-method-create?
  "Does the method correspond to creation?

  By default, httpf returns true for PATCH, POST, and PUT."
  (fn [ctx]
    (methods= ctx #{:patch :post :put})))

(def ^{:group :create :diagram :K03} create-is-method-put?
  "TODO"
  (fn [ctx]
    (method= ctx :put)))

(def ^{:group :create :diagram :L02} create-path?
  "TODO"
  (fn [ctx]
    true))

(def ^{:group :create :diagram :L03} create-partial-put?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :create :diagram :M02} create?
  "TODO"
  (fn [ctx]
    true))

(def ^{:group :create :diagram :M03} create-has-conflict?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :create :diagram :N03} is-create-done?
  "Is the creation done (i.e. not queued up asychronously)?

  By default, httpf returns true."
  (fn [ctx]
    true))

(def ^{:group :create :diagram :N04} create-see-other?
  "TODO"
  (fn [ctx]
    false))

;;; ===== Process =====

(def ^{:group :process :diagram :J10} is-method-head-get?
  "Is the request method equal to HEAD or GET?"
  (fn [ctx]
    (methods= ctx #{:head :get})))

(def ^{:group :process :diagram :J11} is-method-delete?
  "Is the request method equal to DELETE?"
  (fn [ctx]
    (method= ctx :delete)))

(def ^{:group :process :diagram :J13} is-method-put?
  "Is the request method equal to PUT?"
  (fn [ctx]
    (method= ctx :put)))

(def ^{:group :process :diagram :J14} is-method-process?
  "TODO"
  (fn [ctx]
    (method= ctx :process)))

(def ^{:group :process :diagram :K11} process-delete?
  "Did the delete processing succeed?

  By default, httpf returns true.

  Please put the deletion logic in the transformation function, and
  check if it succeeded here in this decision function.

  This may be an important decision to customize."
  (fn [ctx]
    true))

(def ^{:group :process :diagram :K13} process-partial-put?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :process :diagram :K14} process-has-conflict?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :process :diagram :L14} process?
  "TODO"
  (fn [ctx]
    true))

;;; ===== Response =====

(def ^{:group :response :diagram :N08} to-content?
  "Does the response have content?

  By default, httpf returns true unless the method is HEAD. However,
  this behavior may change, since there are many possible reasons for a
  response to not include a message body, including, but not limited to,
  PUT requests.

  This may be an important decision to customize."
  (fn [ctx]
    (not (method= ctx :head))))

(def ^{:group :response :diagram :N09} has-multiple-choices?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :response :diagram :N10} see-other?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :response :diagram :N14} is-process-done?
  "TODO"
  (fn [ctx]
    true))

;;; ===== Alternative =====

(def ^{:group :alternative :diagram :N23} alternative-accept-matches?
  "TODO"
  (fn [ctx]
    true))

(def ^{:group :alternative :diagram :N22} alternative-has-accept?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :alternative :diagram :O21} is-response-alternative?
  "TODO"
  (fn [ctx]
    false))

(def ^{:group :alternative :diagram :N24} alternative-to-content?
  "TODO"
  (fn [ctx]
    true))
