/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.servlet.application;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.Application;
import org.cerberus.entity.MessageEvent;
import org.cerberus.entity.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryApplication;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.service.IApplicationService;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "CreateApplication1", urlPatterns = {"/CreateApplication1"})
public class CreateApplication extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.cerberus.exception.CerberusException
     * @throws org.json.JSONException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_UNEXPECTED_ERROR);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        String application = policy.sanitize(request.getParameter("application"));
        String system = policy.sanitize(request.getParameter("system"));
        String subSystem = policy.sanitize(request.getParameter("subsystem"));
        String type = policy.sanitize(request.getParameter("type"));
        String mavenGpID = policy.sanitize(request.getParameter("mavengroupid"));
        String deployType = policy.sanitize(request.getParameter("deploytype"));
        String svnURL = policy.sanitize(request.getParameter("svnurl"));
        String bugTrackerURL = policy.sanitize(request.getParameter("bugtrackerurl"));
        String newBugURL = policy.sanitize(request.getParameter("bugtrackernewurl"));
        String description = policy.sanitize(request.getParameter("description"));
        Integer sort = 10;
        try {
            if (request.getParameter("sort") != null && !request.getParameter("sort").equals("")) {
                sort = Integer.valueOf(policy.sanitize(request.getParameter("sort")));
            }
        } catch (Exception ex) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Application")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "could not manage to convert sort to an integer value!"));
            ans.setResultMessage(msg);
            jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", ans.getResultMessage().getDescription());
            response.getWriter().print(jsonResponse);
            response.getWriter().flush();
        }

        if (StringUtil.isNullOrEmpty(application)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_EXPECTED_ERROR);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Application")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "application name is missing!"));
            ans.setResultMessage(msg);
        } else {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IApplicationService applicationService = appContext.getBean(IApplicationService.class);
            IFactoryApplication factoryApplication = appContext.getBean(IFactoryApplication.class);

            Application applicationData = factoryApplication.create(application, description, sort, type, system, subSystem, svnURL, deployType, mavenGpID, bugTrackerURL, newBugURL);
            ans = applicationService.createApplication(applicationData);

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);

            try {
                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/CreateApplication", "CREATE", "Create Application : ['" + application + "']", "", ""));
            } catch (CerberusException ex) {
                org.apache.log4j.Logger.getLogger(CreateApplication.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            }
        }

        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(CreateApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CreateApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(CreateApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CreateApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
