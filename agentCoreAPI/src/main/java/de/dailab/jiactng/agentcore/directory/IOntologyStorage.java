package de.dailab.jiactng.agentcore.directory;

import java.net.URI;

import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IServiceDescription;

/**
 * An Interface for the ontology storage that is used by an agentnode to access and cache OWL Ontologies as well as
 * OWL-S Servicedescriptions. Implementing classes are dependent on the specific libraries that are used for the
 * representation of the OWL-Classes and are implemented in other modules.
 * 
 * @author moekon
 */
public interface IOntologyStorage {

  /**
   * Loads a ServiceDescription from a given URI (which may also point to a local file). The Ontology is parsed,
   * searched for services and afterwards an IServiceDescription is created and returned.
   * 
   * @param ontURI
   *          the location of the RDF-Description of the Ontology. May also point to a local file.
   * @return A IServiceDescription object representing the OWL-S service description found in the ontology.
   */
  IServiceDescription loadServiceDescriptionFromOntology(URI ontURI);

  /**
   * Serializes an IServiceDescription into its RDF-representation and returns this as a string.
   * 
   * @param serviceDescription
   *          The ServiceDescription object.
   * @return A String containing the RDF-representation of the service description.
   */
  String serializeServiceDescription(IServiceDescription serviceDescription);

  /**
   * Deserializaes a RDF-String and creates a servicedescription object.
   * 
   * @param serviceString
   *          A String containing the RDF-representation of the service description.
   * @return The ServiceDescription object.
   */
  IServiceDescription deserializeServiceDescription(String serviceString);
  
  /**
   * Integrates the pure information of the action with the semantic service description.
   * 
   * @param action The action description object
   * @param serviceDescription the semantically service description object
   * @return semantic Service Description Object with the IActionDerscriptionInformation
   */
  IServiceDescription mergeServiceDescriptionWithAction(IActionDescription action, IServiceDescription serviceDescription);

  /**
   * Removes an ontology from the local ontologyManager. This might be important for dynamic ontologies, 
   * such as the service templates for matching purposes.
   * 
   * @param ontoURI {@link URI} reference to the ontology
   * @return true if ontology existed before and has now been deleted, false otherwise
   */
  boolean removeOntology(URI ontoURI);
  
  
  /**
   * TODO: java.lang.Object should be replaced with the Ontology-Class, once a definite Library has been found
   */
  // void addOntology(URI ontoURI);
  //
  //
  // Object findOntology(URI ontoURI);
  //
  // void addOntology(Object ont);
  //  
  // void removeOntology(Object ont);
  //  
  // Object findOntology(Object ont);
  //  
  // IServiceDescription getServiceDescriptionFromOntology(Object ont);
}
