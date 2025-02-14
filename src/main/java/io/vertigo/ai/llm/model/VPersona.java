package io.vertigo.ai.llm.model;

/**
 * The persona represents the behavior and specificities of the simulated user.<br>
 * <br>
 * - Name : name of the persona (used for the prompt)<br>
 * - Avatar : avatar of the persona for user presentation<br>
 * - Description : a description for user presentation<br>
 * - Role : the role/job/specialty (used for the prompt), for example "you are a fitness coach"<br>
 * - Context : the context (used for the prompt), for example "you run a fitness center in a small town with limited space for machines"<br>
 * - Style : the style of writing (used for the prompt), for example "you are always positive and encourage people to progress"<br>
 */
// TODO : Add avatar (VFile ? URL ?)
public record VPersona(String name, String description, String role, String context, String style) {

}
