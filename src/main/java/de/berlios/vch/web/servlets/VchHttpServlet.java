package de.berlios.vch.web.servlets;

import java.io.IOException;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.ServiceRegistration;

import de.berlios.vch.web.NotifyMessage;

// TODO think about a central exception handling and automatic
// response rendering (html/ajax) with appropriate http status codes
public abstract class VchHttpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected Dictionary<?, ?> properties;

    protected List<ServiceRegistration<?>> serviceRegs = new LinkedList<ServiceRegistration<?>>();

    protected void unregisterService(ServiceRegistration<?> sr) {
        if (sr != null) {
            sr.unregister();
        }
    }

    protected void unregisterServices() {
        for (ServiceRegistration<?> reg : serviceRegs) {
            unregisterService(reg);
        }
    }

    @Override
    final protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        try {
            get(req, resp);
        } catch (RuntimeException e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                // resp.setContentType("application/json; charset=utf-8");
                resp.setContentType("text/plain; charset=utf-8");
                resp.getWriter().println(e.getLocalizedMessage());
            } else {
                throw e;
            }
        }
    }

    @Override
    final protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        try {
            post(req, resp);
        } catch (RuntimeException e) {
            if (isAjaxRequest(req)) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                // resp.setContentType("application/json; charset=utf-8");
                resp.setContentType("text/plain; charset=utf-8");
                resp.getWriter().println(e.getLocalizedMessage());
            } else {
                throw e;
            }
        }
    }

    private boolean isAjaxRequest(HttpServletRequest req) {
        return "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
    }

    protected abstract void get(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

    protected abstract void post(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException;

    private static final String NOTIFY_MESSAGES = "notifyMessages";

    public void addNotify(HttpServletRequest req, NotifyMessage msg) {
        getNotifyMessages(req).add(msg);
    }

    public List<NotifyMessage> getNotifyMessages(HttpServletRequest req) {
        @SuppressWarnings("unchecked")
        List<NotifyMessage> msgs = (List<NotifyMessage>) req.getAttribute(NOTIFY_MESSAGES);
        if (msgs == null) {
            msgs = new LinkedList<NotifyMessage>();
            req.setAttribute(NOTIFY_MESSAGES, msgs);
        }
        return msgs;
    }
}
