package io.mrizzi.jms;

import org.jboss.logging.Logger;
import org.jboss.windup.web.services.json.WindupExecutionJSONUtil;
import org.jboss.windup.web.services.model.AdvancedOption;
import org.jboss.windup.web.services.model.AnalysisContext;
import org.jboss.windup.web.services.model.ExecutionState;
import org.jboss.windup.web.services.model.MigrationPath;
import org.jboss.windup.web.services.model.PathType;
import org.jboss.windup.web.services.model.RegisteredApplication;
import org.jboss.windup.web.services.model.RulesPath;
import org.jboss.windup.web.services.model.Technology;
import org.jboss.windup.web.services.model.WindupExecution;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class AnalysisExecutionProducer {

    private static final Logger LOG = Logger.getLogger(AnalysisExecutionProducer.class);

    @Inject
    ConnectionFactory connectionFactory;

    public void triggerAnalysis(String applicationId) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            TextMessage executionRequestMessage = context.createTextMessage();

            executionRequestMessage.setLongProperty("projectId", Long.parseLong(applicationId));
            long executionId = System.currentTimeMillis();
            executionRequestMessage.setLongProperty("executionId", executionId);

            AnalysisContext analysisContext = new AnalysisContext();
            analysisContext.setGenerateStaticReports(false);
//            analysisContext.setCloudTargetsIncluded(true);

/*
            MigrationPath migrationPath = new MigrationPath();
            Technology targetEAP = new Technology();
            targetEAP.setName("eap7");
            migrationPath.setTarget(targetEAP);
            analysisContext.setMigrationPath(migrationPath);
*/

            analysisContext.setAdvancedOptions(Stream.of("eap7", "cloud-readiness", "quarkus", "rhr").map(targetValue -> new AdvancedOption("target", targetValue)).collect(Collectors.toList()));

            RulesPath rulesPath = new RulesPath();
            rulesPath.setPath("/opt/mta-cli/rules");
            rulesPath.setScanRecursively(true);
            rulesPath.setRulesPathType(PathType.SYSTEM_PROVIDED);
            analysisContext.setRulesPaths(Collections.singleton(rulesPath));

            RegisteredApplication registeredApplication = new RegisteredApplication();
//            registeredApplication.setInputPath("/opt/eap/standalone/data/input/jee-example-app-1.0.0.ear");
            registeredApplication.setInputPath("/home/mrizzi/Tools/windup/sample/input/jee-example-app-1.0.0.ear");
            analysisContext.setApplications(Set.of(registeredApplication));

            WindupExecution windupExecution = new WindupExecution();
            windupExecution.setId(executionId);
            windupExecution.setAnalysisContext(analysisContext);
            windupExecution.setTimeQueued(new GregorianCalendar());
            windupExecution.setState(ExecutionState.QUEUED);
            windupExecution.setOutputPath(Path.of("/home/mrizzi/Tools/windup/sample/output/prototype", applicationId, Long.toString(executionId)).toString());

            String json = WindupExecutionJSONUtil.serializeToString(windupExecution);
            executionRequestMessage.setText(json);
            LOG.infof("Going to send the Windup execution request %s", json);
            context.createProducer().send(context.createQueue("executorQueue"), executionRequestMessage);
        }
        catch (JMSException | IOException e)
        {
            throw new RuntimeException("Failed to create WindupExecution stream message!", e);
        }
    }
}
