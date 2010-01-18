package de.dailab.jiactng.agentcore.directory;

import java.util.ArrayList;

import de.dailab.jiactng.agentcore.ontology.IServiceDescription;

/**
 * Interface for the servicematcher. This interface is used by the agentcore to make generic operations on a
 * servicematcher. The servicematcher itself is implemented in another module and is dependent on the specific libraries
 * that are used to represent the ontologies.
 * 
 * @author moekon
 */
public interface IServiceMatcher {

  /**
   * This method starts a matching process with the implementing service matcher and returns the best result.
   * 
   * @param serviceTemplate
   *          A template for the matcher that is used to find applicable services.
   * @param serviceDescList
   *          The list of services (from the directory) that the matcher should use to find a match.
   * @return The serviceDescription that is the best match for the template.
   */
  IServiceDescription findBestMatch(IServiceDescription serviceTemplate,
      ArrayList<IServiceDescription> serviceDescList);

  /**
   * This method starts a matching process with the implementing service matcher and returns all results.
   * 
   * @param serviceTemplate
   *          A template for the matcher that is used to find applicable services.
   * @param serviceDescList
   *          The list of services (from the directory) that the matcher should use to find a match.
   * @return A list of serviceDescriptions that match the template and are ordered by their quality.
   */
  ArrayList<IServiceDescription> findAllMatches(IServiceDescription serviceTemplate,
      ArrayList<IServiceDescription> serviceDescList);

}
