(ns httpf.handlers
  (:require
   [httpf.utility :refer [delegate-response]]))

;;; ===== 1xx =====

(defn continue
  "'The 100 (Continue) status code indicates that the initial part of a
  request has been received and has not yet been rejected by the server.
  The server intends to send a final response after the request has been
  fully received and acted upon.

  When the request contains an Expect header field that includes a
  100-continue expectation, the 100 response indicates that the server
  wishes to receive the request payload body, as described in Section
  5.1.1. The client ought to continue sending the request and discard
  the 100 response.

  If the request did not contain an Expect header field containing the
  100-continue expectation, the client can simply discard this interim
  response.' - RFC 7231"
  [ctx]
  (delegate-response ctx 100 "Continue" :continue))

(defn switching-protocols
  "'The 101 (Switching Protocols) status code indicates that the server
  understands and is willing to comply with the client's request, via
  the Upgrade header field (Section 6.7 of [RFC7230]), for a change in
  the application protocol being used on this connection. The server
  MUST generate an Upgrade header field in the response that indicates
  which protocol(s) will be switched to immediately after the empty line
  that terminates the 101 response.

  It is assumed that the server will only agree to switch protocols when
  it is advantageous to do so. For example, switching to a newer version
  of HTTP might be advantageous over older versions, and switching to a
  real-time, synchronous protocol might be advantageous when delivering
  resources that use such features.' - RFC 7231"
  [ctx]
  (delegate-response ctx 101 "Switching Protocols" :switching-protocols))

;;; ===== 2xx =====

(defn ok
  "'The 200 (OK) status code indicates that the request has succeeded.
  The payload sent in a 200 response depends on the request method. For
  the methods defined by this specification, the intended meaning of the
  payload can be summarized as:

  GET a representation of the target resource;

  HEAD the same representation as GET, but without the representation
  data;

  POST a representation of the status of, or results obtained from, the
  action;

  PUT, DELETE a representation of the status of the action;

  OPTIONS a representation of the communications options;

  TRACE a representation of the request message as received by the end
  server.

  Aside from responses to CONNECT, a 200 response always has a payload,
  though an origin server MAY generate a payload body of zero length.
  If no payload is desired, an origin server ought to send 204 (No
  Content) instead. For CONNECT, no payload is allowed because the
  successful result is a tunnel, which begins immediately after the 200
  response header section.

  A 200 response is cacheable by default; i.e., unless otherwise
  indicated by the method definition or explicit cache controls (see
  Section 4.2.2 of [RFC7234]).' - RFC 7231"
  [ctx]
  (delegate-response ctx 200 "OK" :ok))

(defn created
  "'The 201 (Created) status code indicates that the request has been
  fulfilled and has resulted in one or more new resources being created.
  The primary resource created by the request is identified by either a
  Location header field in the response or, if no Location field is
  received, by the effective request URI.

  The 201 response payload typically describes and links to the
  resource(s) created. See Section 7.2 for a discussion of the meaning
  and purpose of validator header fields, such as ETag and
  Last-Modified, in a 201 response.' - RFC 7231"
  [ctx]
  (delegate-response ctx 201 "Created" :created))

(defn accepted
  "'The 202 (Accepted) status code indicates that the request has been
  accepted for processing, but the processing has not been completed.
  The request might or might not eventually be acted upon, as it might
  be disallowed when processing actually takes place. There is no
  facility in HTTP for re-sending a status code from an asynchronous
  operation.

  The 202 response is intentionally noncommittal. Its purpose is to
  allow a server to accept a request for some other process (perhaps a
  batch-oriented process that is only run once per day) without
  requiring that the user agent's connection to the server persist until
  the process is completed. The representation sent with this response
  ought to describe the request's current status and point to
  (or embed) a status monitor that can provide the user with an estimate
  of when the request will be fulfilled.' - RFC 7231"
  [ctx]
  (delegate-response ctx 202 "Accepted" :accepted))

(defn no-content
  "Returns a response for 204 (No Content).

  Currently, if :no-content is customized, it is responsible for
  returning a nil body.

  TODO: Should the correct behavior should be enforced?

  'The 204 (No Content) status code indicates that the server has
  successfully fulfilled the request and that there is no additional
  content to send in the response payload body. Metadata in the response
  header fields refer to the target resource and its selected
  representation after the requested action was applied.

  For example, if a 204 status code is received in response to a PUT
  request and the response contains an ETag header field, then the PUT
  was successful and the ETag field-value contains the entity-tag for
  the new representation of that target resource.

  The 204 response allows a server to indicate that the action has been
  successfully applied to the target resource, while implying that the
  user agent does not need to traverse away from its current 'document
  view' (if any). The server assumes that the user agent will provide
  some indication of the success to its user, in accord with its own
  interface, and apply any new or updated metadata in the response to
  its active representation.

  For example, a 204 status code is commonly used with document editing
  interfaces corresponding to a 'save' action, such that the document
  being saved remains available to the user for editing. It is also
  frequently used with interfaces that expect automated data transfers
  to be prevalent, such as within distributed version control systems.

  A 204 response is terminated by the first empty line after the header
  fields because it cannot contain a message body.

  A 204 response is cacheable by default; i.e., unless otherwise
  indicated by the method definition or explicit cache controls (see
  Section 4.2.2 of [RFC7234]).' - RFC 7231"
  [ctx]
  (delegate-response ctx 204 nil :no-content))

;;; ===== 3xx =====

(defn multiple-choices
  [ctx]
  (delegate-response ctx 300 "Multiple Choices" :multiple-choices))

(defn see-other
  [ctx]
  (delegate-response ctx 303 "See Other" :see-other))

(defn not-modified
  [ctx]
  (delegate-response ctx 304 "Not Modified" :not-modified))

(defn temporary-redirect
  [ctx]
  (delegate-response ctx 307 "Temporary Redirect" :temporary-redirect))

(defn permanent-redirect
  [ctx]
  (delegate-response ctx 308 "Permanent Redirect" :permanent-redirect))

;;; ===== 4xx =====

(defn bad-request
  "'The 400 (Bad Request) status code indicates that the server cannot
  or will not process the request due to something that is perceived to
  be a client error (e.g., malformed request syntax, invalid request
  message framing, or deceptive request routing).' - RFC 7231"
  [ctx]
  (delegate-response ctx 400 "Bad Request" :bad-request))

(defn unauthorized
  "'The 401 (Unauthorized) status code indicates that the request has
  not been applied because it lacks valid authentication credentials for
  the target resource. The server generating a 401 response MUST send a
  WWW-Authenticate header field (Section 4.1) containing at least one
  challenge applicable to the target resource.

  If the request included authentication credentials, then the 401
  response indicates that authorization has been refused for those
  credentials. The user agent MAY repeat the request with a new or
  replaced Authorization header field (Section 4.2). If the 401 response
  contains the same challenge as the prior response, and the user agent
  has already attempted authentication at least once, then the user
  agent SHOULD present the enclosed representation to the user, since it
  usually contains relevant diagnostic information.' - RFC 7235"
  [ctx]
  (delegate-response ctx 401 "Unauthorized" :unauthorized))

(defn forbidden
  "'The 403 (Forbidden) status code indicates that the server understood
  the request but refuses to authorize it. A server that wishes to make
  public why the request has been forbidden can describe that reason in
  the response payload (if any).

  If authentication credentials were provided in the request, the server
  considers them insufficient to grant access. The client SHOULD NOT
  automatically repeat the request with the same credentials. The client
  MAY repeat the request with new or different credentials. However, a
  request might be forbidden for reasons unrelated to the credentials.'

  An origin server that wishes to 'hide' the current existence of a
  forbidden target resource MAY instead respond with a status code of
  404 (Not Found).' - RFC 7231"
  [ctx]
  (delegate-response ctx 403 "Forbidden" :forbidden))

(defn not-found
  "'The 404 (Not Found) status code indicates that the origin server did
  not find a current representation for the target resource or is not
  willing to disclose that one exists. A 404 status code does not
  indicate whether this lack of representation is temporary or
  permanent; the 410 (Gone) status code is preferred over 404 if the
  origin server knows, presumably through some configurable means, that
  the condition is likely to be permanent.

  A 404 response is cacheable by default; i.e., unless otherwise
  indicated by the method definition or explicit cache controls (see
  Section 4.2.2 of [RFC7234]).' - RFC 7231"
  [ctx]
  (delegate-response ctx 404 "Not Found" :not-found))

(defn method-not-allowed
  "'The 405 (Method Not Allowed) status code indicates that the method
  received in the request-line is known by the origin server but not
  supported by the target resource.  The origin server MUST generate an
  Allow header field in a 405 response containing a list of the target
  resource's currently supported methods.

  A 405 response is cacheable by default; i.e., unless otherwise
  indicated by the method definition or explicit cache controls (see
  Section 4.2.2 of [RFC7234]).' - RFC 7231"
  [ctx]
  (delegate-response ctx 405 "Method Not Allowed" :method-not-allowed))

(defn not-acceptable
  [ctx]
  (delegate-response ctx 406 "Not Acceptable" :not-acceptable))

(defn conflict
  [ctx]
  (delegate-response ctx 409 "Conflict" :conflict))

(defn gone
  [ctx]
  (delegate-response ctx 410 "Gone" :gone))

(defn precondition-failed
  [ctx]
  (delegate-response ctx 412 "Precondition Failed" :precondition-failed))

(defn payload-too-large
  [ctx]
  (delegate-response ctx 413 "Payload Too Large" :payload-too-large))

(defn uri-too-long
  [ctx]
  (delegate-response ctx 414 "URI Too Long" :uri-too-long))

(defn unsupported-media-type
  [ctx]
  (delegate-response ctx 415 "Unsupported Media Type" :unsupported-media-type))

(defn expectation-failed
  [ctx]
  (delegate-response ctx 417 "Expectation Failed" :expectation-failed))

(defn unprocessable-entity
  "'The 422 (Unprocessable Entity) status code means the server
  understands the content type of the request entity (hence a
  415 (Unsupported Media Type) status code is inappropriate), and the
  syntax of the request entity is correct (thus a 400 (Bad Request)
  status code is inappropriate) but was unable to process the contained
  instructions.  For example, this error condition may occur if an XML
  request body contains well-formed (i.e., syntactically correct), but
  semantically erroneous, XML instructions.' - RFC 4918"
  [ctx]
  (delegate-response ctx 422 "Unprocessable Entity" :unprocessable-entity))

(defn request-header-fields-too-large
  [ctx]
  (delegate-response ctx 431 "Request Header Fields Too Large"
                     :request-header-fields-too-large))

;;; ===== 5xx =====

(defn internal-server-error
  [ctx]
  (delegate-response ctx 500 "Internal Server Error" :internal-server-error))

(defn not-implemented
  [ctx]
  (delegate-response ctx 501 "Not Implemented" :not-implemented))

(defn service-unavailable
  [ctx]
  (delegate-response ctx 503 "Service Unavailable" :service-unavailable))
