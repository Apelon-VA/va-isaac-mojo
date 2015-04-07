package gov.vha.isaac.mojo.hk2;

import gov.vha.isaac.ochre.api.LookupService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Created by kec on 9/6/14.
 */
@Mojo( name = "set-run-level")
public class SetRunLevel extends AbstractMojo {

    @Parameter(required = true)
    String runLevel;

    @Override
    public void execute()
            throws MojoExecutionException {
        LookupService.getRunLevelController().proceedTo(Integer.valueOf(runLevel));
    }
}
