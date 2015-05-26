/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright 
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.mojo.termstore.transforms;

import gov.vha.isaac.ochre.api.LookupService;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;

/**
 * Goal which executes arbitrary transforms.
 */
@Mojo (defaultPhase = LifecyclePhase.PROCESS_RESOURCES, name = "execute-transforms")
public class TransformExecutor extends AbstractMojo
{

	/**
	 * The transforms and their configuration that are to be executed
	 */
	@Parameter (required = true)
	private Transform[] transforms;
	
	/**
	 * The folder where any summary output files should be written
	 */
	@Parameter (required = true)
	private File summaryOutputFolder;

	
	/**
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		summaryOutputFolder.mkdirs();
		StringBuilder summaryInfo = new StringBuilder();
		try
		{
			getLog().info("Executing DB Transforms");
			
			ArrayList<TransformConceptIterateI> iterateTransforms = new ArrayList<>();
			
			TerminologyStoreDI store = LookupService.getService(TerminologyStoreDI.class);

			for (Transform t : transforms)
			{
				long start = System.currentTimeMillis();
				TransformI transformer = LookupService.getService(TransformI.class, t.getName());
				if (transformer == null)
				{
					throw new MojoExecutionException("Could not locate a TransformI implementation with the name '" + t.getName() + "'.");
				}
				if (transformer instanceof TransformArbitraryI)
				{
					getLog().info("Executing arbitrary transform " + transformer.getName() + " - " + transformer.getDescription());
					transformer.configure(t.getConfigFile(), store);
					
					((TransformArbitraryI)transformer).transform(store);
					String summary = "Transformer " + t.getName() + " completed:  " + transformer.getWorkResultSummary() + " in " + (System.currentTimeMillis() - start) + "ms";
					getLog().info(summary);
					summaryInfo.append(summary);
					summaryInfo.append(System.getProperty("line.separator"));
				}
				else if (transformer instanceof TransformConceptIterateI)
				{
					iterateTransforms.add((TransformConceptIterateI)transformer);
					transformer.configure(t.getConfigFile(), store);
				}
				else
				{
					throw new MojoExecutionException("Unhandled transform subtype");
				}
			}
			
			if (iterateTransforms.size() > 0)
			{
				getLog().info("Executing concept iterate transforms:");
				for (TransformConceptIterateI it : iterateTransforms)
				{
					getLog().info(it.getName() + " - " + it.getDescription());
				}
				
				AtomicInteger parallelChangeCount = new AtomicInteger();
				long start = System.currentTimeMillis();
				
				//Start a process to iterate all concepts in the DB.
				store.getParallelConceptStream().forEach((cc) ->
				{
					for (TransformConceptIterateI it : iterateTransforms)
					{
						try
						{
							if (it.transform(store, cc))
							{
								int last = parallelChangeCount.getAndIncrement();
								//commit every 2000 changes (this is running in parallel)
								if (last % 2000 == 0)
								{
									store.commit();
								}
							}
						}
						catch (Exception e)
						{
							throw new RuntimeException(e);
						}
					}
				});
				
				store.commit();
				getLog().info("Parallel concept iterate completed in " + (System.currentTimeMillis() - start) + "ms.");
				summaryInfo.append("Parallel concept iterate completed in " + (System.currentTimeMillis() - start) + "ms.");
				for (TransformConceptIterateI it : iterateTransforms)
				{
					it.writeSummaryFile(summaryOutputFolder);
					
					String summary = "Transformer " + it.getName() + " completed:  " + it.getWorkResultSummary();
					getLog().info(summary);
					summaryInfo.append(summary);
					summaryInfo.append(System.getProperty("line.separator"));
				}
			}
			
			Files.write(new File(summaryOutputFolder, "transformsSummary.txt").toPath(), summaryInfo.toString().getBytes());
			
			getLog().info("Finished executing transforms");
		}
		catch (Exception e)
		{
			throw new MojoExecutionException("Database transform failure", e);
		}
	}
}
