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

        if (datastoreRootLocation != null) {
            System.setProperty(Constants.DATA_STORE_ROOT_LOCATION_PROPERTY, datastoreRootLocation);
        }
        if (termstoreRootLocation != null) {
            System.setProperty(Constants.CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY, termstoreRootLocation);
        }
        if (searchRootLocation != null) {
            System.setProperty(Constants.SEARCH_ROOT_LOCATION_PROPERTY, searchRootLocation);
        }
        if (StringUtils.isNotBlank(conceptModel)) {
            LookupService.getService(ConfigurationService.class).setConceptModel(ConceptModel.valueOf(conceptModel));
        }
    }
}
