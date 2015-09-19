package gov.vha.isaac.mojo.properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import gov.vha.isaac.ochre.api.ConceptModel;
import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.constants.Constants;
import java.nio.file.Paths;

/**
 * Created by kec on 9/13/14.
 */

@Mojo( name = "set-termstore-properties",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class SetTermstoreProperties extends AbstractMojo {

    @Parameter
    String termstoreRootLocation;

    @Parameter
    String searchRootLocation;
    
    @Parameter
    String datastoreRootLocation;
    
    /**
     * Must be set to one of the enum constants from {@link ConceptModel}
     */
    @Parameter
    String conceptModel;

    @Override
    public void execute() throws MojoExecutionException {
        System.getProperties().remove(Constants.DATA_STORE_ROOT_LOCATION_PROPERTY);
        System.getProperties().remove(Constants.CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY);
        System.getProperties().remove(Constants.SEARCH_ROOT_LOCATION_PROPERTY);

        assert System.getProperties().containsKey(Constants.DATA_STORE_ROOT_LOCATION_PROPERTY) == false;
        assert System.getProperties().containsKey(Constants.CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY) == false;
        assert System.getProperties().containsKey(Constants.SEARCH_ROOT_LOCATION_PROPERTY) == false;

        ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
        if (configurationService == null) {
            throw new RuntimeException("No implementation of ConfigurationService available.  You probably need newtons-cradle on your classpath");
        }
        if (datastoreRootLocation != null) {
            System.setProperty(Constants.DATA_STORE_ROOT_LOCATION_PROPERTY, datastoreRootLocation);
            getLog().info("Datastore Root: " + datastoreRootLocation);
            getLog().info("Datastore Root[2]: " + configurationService.getDataStoreFolderPath());
            configurationService.setDataStoreFolderPath(Paths.get(datastoreRootLocation));
            getLog().info("Datastore Root[3]: " + configurationService.getDataStoreFolderPath());
        }
        if (termstoreRootLocation != null) {
            System.setProperty(Constants.CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY, termstoreRootLocation);
            getLog().info("Termstore Root: " + datastoreRootLocation);
        }
        if (searchRootLocation != null) {
            System.setProperty(Constants.SEARCH_ROOT_LOCATION_PROPERTY, searchRootLocation);
            getLog().info("Search Root: " + datastoreRootLocation);
        }
        if (StringUtils.isNotBlank(conceptModel)) {
            configurationService.setConceptModel(ConceptModel.valueOf(conceptModel));
       }
       getLog().info("chronicle folder path: " + configurationService.getChronicleFolderPath());
     }
}
