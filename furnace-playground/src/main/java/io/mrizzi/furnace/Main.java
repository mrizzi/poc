package io.mrizzi.furnace;

import org.jboss.forge.addon.maven.projects.MavenBuildSystem;
import org.jboss.forge.addon.parser.java.projects.JavaWebProjectType;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.templates.Template;
import org.jboss.forge.addon.templates.TemplateFactory;
import org.jboss.forge.addon.templates.freemarker.FreemarkerTemplate;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.forge.furnace.spi.ServiceRegistry;
import org.jboss.forge.furnace.util.Addons;
import org.jboss.forge.furnace.util.OperatingSystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.Future;

/**
 * This is an example on how to start Furnace in a standalone app
 *
 */
public class Main
{

    public static void main(String[] args) throws Exception
    {
        Furnace furnace = startFurnace();
        try
        {
            System.out.println("furnace.getStatus() " + furnace.getStatus());
            System.out.println("furnace.getRuntimeClassLoader() " + furnace.getRuntimeClassLoader());
            AddonRegistry addonRegistry = furnace.getAddonRegistry();
            System.out.println("addonRegistry.getVersion() " + addonRegistry.getVersion());
            for (Addon addon : addonRegistry.getAddons()) {
                ServiceRegistry serviceRegistry = addon.getServiceRegistry();
                System.out.printf("Addon %s (%s) is started? %s\n", addon.getId(), serviceRegistry.getExportedInstance(ProjectFactory.class), addon.getStatus());
            }
/*
            for (Class<?> clazz : addonRegistry.getExportedTypes()) {
                System.out.printf("ExportedTypes %s\n", clazz.getName());
            }
*/
/*
            AddonId projects = AddonId.from("org.jboss.forge.addon:projects", "3.9.8.Final");
            Addons.waitUntilStarted(addonRegistry.getAddon(projects));
*/
            ProjectFactory projectFactory = null;
            while (projectFactory == null) {
                try {
                    Thread.sleep(1000);
                    projectFactory = addonRegistry.getServices(ProjectFactory.class).get();
                } catch (Exception e) {
                    System.out.printf("Exception %s\n", e.getMessage());
                }
            }
            createProject(addonRegistry);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally
        {
            furnace.stop();
        }
    }

    static Furnace startFurnace() throws Exception
    {
        // Create a Furnace instance. NOTE: This must be called only once
        Furnace furnace = FurnaceFactory.getInstance();

        // Add repository containing addons specified in pom.xml
        furnace.addRepository(AddonRepositoryMode.IMMUTABLE, new File("target/addons"));

        // Start Furnace in another thread
        Future<Furnace> future = furnace.startAsync();

        // Wait until Furnace is started and return
        return future.get();
    }

    private static void createProject(AddonRegistry addonRegistry)
    {
        System.out.println("createProject - addonRegistry.getVersion() " + addonRegistry.getVersion());
        ProjectFactory projectFactory = addonRegistry.getServices(ProjectFactory.class).get();
        ResourceFactory resourceFactory = addonRegistry.getServices(ResourceFactory.class).get();

        // Create a temporary directory as an example
        File underlyingResource = OperatingSystemUtils.createTempDir();

        Resource<File> projectDir = resourceFactory.create(underlyingResource);

        // We want it to be a Maven project, so we need the maven-api class in here
        MavenBuildSystem projectProvider = addonRegistry.getServices(MavenBuildSystem.class).get();

        // Creating WAR project
        JavaWebProjectType javaWebProjectType = addonRegistry.getServices(JavaWebProjectType.class).get();
        Project project = projectFactory.createProject(projectDir, projectProvider,
                javaWebProjectType.getRequiredFacets());

        // Changing metadata
        MetadataFacet facet = project.getFacet(MetadataFacet.class);
        facet.setProjectName("my-demo-project");
        facet.setProjectVersion("1.0.0-SNAPSHOT");
        facet.setProjectGroupName("com.mycompany.project");

        System.out.println("Project Created in: " + project);
    }

    private static void createTemplate(AddonRegistry addonRegistry) throws Exception
    {
        for (Addon addon : addonRegistry.getAddons()) {
            ServiceRegistry serviceRegistry = addon.getServiceRegistry();
            System.out.printf("createTemplate - Addon %s (%s) is started? %s\n", addon.getId(), serviceRegistry.getExportedInstance(ResourceFactory.class), addon.getStatus());
        }
        ResourceFactory resourceFactory = addonRegistry.getServices(ResourceFactory.class).get();
        TemplateFactory templateFactory = addonRegistry.getServices(TemplateFactory.class).get();
        File tmpFile = File.createTempFile("template", ".tmp");
        tmpFile.deleteOnExit();
        Files.write(tmpFile.toPath(), "${name}".getBytes());
        Template template = templateFactory.create(resourceFactory.create(tmpFile), FreemarkerTemplate.class);
        String output = template.process(Collections.singletonMap("name", "JBoss Forge"));
        System.out.println("Output: " + output);
    }
}