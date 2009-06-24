package de.dailab.jiactng.agentcore.ontology;

/**
 * Interface class for OWL-S based service descriptions. These descriptions extend the normal ActionDescriptions from
 * agentcore. The implementing classes are bound to specific owl-libraries, so this interface is used by the agentcore
 * to enable a generic handling of such service descriptions.
 * 
 * @author moekon
 * @see de.dailab.jiactng.agentcore.action.IActionDescription
 */
public interface IServiceDescription extends IActionDescription {

  /**
   * Getter for the raw sourcecode of the ontology in which the servicedescription is defined. This is usually an
   * RDF-Document. This Method is used for serialization/deserialization.
   * 
   * @return A String containing the RDF-Representation of the ServiceDescriptions ontology.
   */
  public String getOntologySource();

  /**
   * Setter for the raw sourcecode of the ontology in which the servicedescription is defined. This is usually an
   * RDF-Document. This Method is used for serialization/deserialization.
   * 
   * @param ontologySource
   *          A String containing the RDF-Representation of the ServiceDescriptions ontology.
   */
  public void setOntologySource(String ontologySource);

}
