/*
 * Copyright 2014 Informatics, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.mojo;

import gov.vha.isaac.metadata.source.IsaacMetadataAuxiliaryBinding;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.spec.PathSpec;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntMemberChronicle;

/**
 *
 * @author aimeefurber
 * @author dylangrald
 */
@Mojo(name = "create-path-econcept",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class PathEConcept extends AbstractMojo {

    private static final String DIR = System.getProperty("user.dir");

    private static final Logger LOGGER = Logger.getLogger(PathEConcept.class.getName());


    /**
     * Paths to add to initial database.
     *
     */
    @Parameter
    private PathSpec[] initialPaths;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            File folders = new File(DIR + "/target/generated-resources");
            if (!folders.exists()) {
                folders.mkdirs();
            }
            File out = new File(DIR + "/target/generated-resources/pathEConcept.jbin");
            createPathEConcepts(initialPaths, out);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception writing EConcept file", ex);
            throw new MojoFailureException(ex.getMessage());
        }
    }

    private static void createPathEConcepts(PathSpec[] initialPaths, File out) throws IOException {
        List<TtkRefexAbstractMemberChronicle<?>> pathMembers = new ArrayList<>();
        List<TtkRefexAbstractMemberChronicle<?>> originMembers = new ArrayList<>();

        for (PathSpec spec : initialPaths) {
            long startTime = System.currentTimeMillis();
            TtkRefexUuidMemberChronicle pathMember = new TtkRefexUuidMemberChronicle();
            pathMember.primordialUuid = UUID.randomUUID();
            pathMember.referencedComponentUuid = pathMember.primordialUuid;
            pathMember.setStatus(Status.ACTIVE);
            pathMember.setTime(startTime);
            pathMember.setAuthorUuid(IsaacMetadataAuxiliaryBinding.USER.getPrimodialUuid());
            pathMember.setModuleUuid(IsaacMetadataAuxiliaryBinding.ISAAC_MODULE.getPrimodialUuid());
            pathMember.setPathUuid(IsaacMetadataAuxiliaryBinding.MASTER.getPrimodialUuid());
            pathMember.setAssemblageUuid(IsaacMetadataAuxiliaryBinding.PATHS.getPrimodialUuid());
            pathMember.setUuid1(spec.getPathConcept().getUuids()[0]);
            pathMembers.add(pathMember);

            TtkRefexUuidIntMemberChronicle originMember = new TtkRefexUuidIntMemberChronicle();
            originMember.primordialUuid = UUID.randomUUID();
            originMember.referencedComponentUuid = originMember.primordialUuid;
            originMember.setStatus(Status.ACTIVE);
            originMember.setTime(startTime);
            originMember.setAuthorUuid(IsaacMetadataAuxiliaryBinding.USER.getPrimodialUuid());
            originMember.setModuleUuid(IsaacMetadataAuxiliaryBinding.ISAAC_MODULE.getPrimodialUuid());
            originMember.setPathUuid(IsaacMetadataAuxiliaryBinding.MASTER.getPrimodialUuid());
            originMember.setAssemblageUuid(IsaacMetadataAuxiliaryBinding.PATH_ORIGINS.getPrimodialUuid());
            originMember.setUuid1(spec.getOriginConcept().getUuids()[0]);
            originMember.setInt1(Integer.MAX_VALUE);
            originMembers.add(originMember);
        }

        TtkConceptChronicle pathRefsetConcept = new TtkConceptChronicle();
        pathRefsetConcept.setPrimordialUuid(IsaacMetadataAuxiliaryBinding.PATHS.getPrimodialUuid());
        pathRefsetConcept.setRefsetMembers(pathMembers);

        TtkConceptChronicle originRefsetConcept = new TtkConceptChronicle();
        originRefsetConcept.setPrimordialUuid(IsaacMetadataAuxiliaryBinding.PATH_ORIGINS.getPrimodialUuid());
        originRefsetConcept.setRefsetMembers(originMembers);

        //write to file (will merge with existing concepts, assumes that Path and Origin concepts have been loaded)
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out)))) {
            pathRefsetConcept.writeExternal(dos);
            originRefsetConcept.writeExternal(dos);
            dos.flush();
        }
    }

}
