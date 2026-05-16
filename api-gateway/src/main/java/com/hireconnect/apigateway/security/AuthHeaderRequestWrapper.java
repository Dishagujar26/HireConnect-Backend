package com.hireconnect.apigateway.security;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/*
 * Strips any incoming X-Auth-* headers (preventing header injection by clients)
 * and replaces them with gateway-verified values extracted from the JWT.
 *
 * Multipart methods (getPart, getParts, getInputStream) are explicitly delegated
 * so that file uploads (e.g. resume PDF via multipart/form-data) are not silently
 * dropped when passing through this filter.
 */
public class AuthHeaderRequestWrapper extends HttpServletRequestWrapper {

    private static final String USER_ID_HEADER = "X-Auth-User-Id";
    private static final String EMAIL_HEADER   = "X-Auth-User-Email";
    private static final String ROLE_HEADER    = "X-Auth-User-Role";

    private final Map<String, String> customHeaders = new HashMap<>();

    public AuthHeaderRequestWrapper(HttpServletRequest request, Long userId, String email, String role) {
        super(request);
        customHeaders.put(USER_ID_HEADER, String.valueOf(userId));
        customHeaders.put(EMAIL_HEADER,   email);
        customHeaders.put(ROLE_HEADER,    role);
    }
    /**
     * Retrieves header.
     *
     * @author Disha Gujar
     */

    @Override
    public String getHeader(String name) {
        if (customHeaders.containsKey(name)) {
            return customHeaders.get(name);
        }
        if (isTrustedHeader(name)) {
            return null;
        }
        return super.getHeader(name);
    }
    /**
     * Retrieves headers.
     *
     * @author Disha Gujar
     */

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (customHeaders.containsKey(name)) {
            return Collections.enumeration(List.of(customHeaders.get(name)));
        }
        if (isTrustedHeader(name)) {
            return Collections.emptyEnumeration();
        }
        return super.getHeaders(name);
    }
    /**
     * Retrieves header names.
     *
     * @author Disha Gujar
     */

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new HashSet<>();
        Enumeration<String> original = super.getHeaderNames();
        while (original.hasMoreElements()) {
            String h = original.nextElement();
            if (!isTrustedHeader(h)) names.add(h);
        }
        names.addAll(customHeaders.keySet());
        return Collections.enumeration(names);
    }

    // ── Multipart delegation ─────────────────────────────────────────────────
    // Without these overrides the servlet container loses the multipart context
    // and Spring's MultipartFile resolution fails with an empty/null file.
    /**
     * Retrieves part.
     *
     * @author Disha Gujar
     */

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return super.getPart(name);
    }
    /**
     * Retrieves parts.
     *
     * @author Disha Gujar
     */

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return super.getParts();
    }

    /**
     * Delegates to the underlying request's ServletInputStream so the raw
     * request body (including multipart form-data for file uploads) is readable
     * by the downstream service via the gateway proxy.
     *
     * isFinished() correctly tracks EOF so the servlet container knows when
     * the stream has been fully consumed (avoids IO hangs on some containers).
     
 * @author Disha Gujar
 */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        InputStream delegate = super.getInputStream();
        return new ServletInputStream() {
            private boolean finished = false;
    /**
     * Read.
     *
     * @author Disha Gujar
     */

            @Override
            public int read() throws IOException {
                int b = delegate.read();
                if (b == -1) finished = true;
                return b;
            }
    /**
     * Read.
     *
     * @author Disha Gujar
     */

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int n = delegate.read(b, off, len);
                if (n == -1) finished = true;
                return n;
            }

            @Override public boolean isFinished() { return finished; }
            @Override public boolean isReady()    { return true;  }
            @Override public void setReadListener(ReadListener listener) { /* no-op for sync processing */ }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────

    private boolean isTrustedHeader(String name) {
        return USER_ID_HEADER.equalsIgnoreCase(name)
            || EMAIL_HEADER.equalsIgnoreCase(name)
            || ROLE_HEADER.equalsIgnoreCase(name);
    }
}
