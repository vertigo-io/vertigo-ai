package io.vertigo.ai.impl.llm;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.vertigo.ai.llm.model.VPrompt;
import io.vertigo.datafactory.collections.model.Facet;
import io.vertigo.datafactory.collections.model.FacetValue;
import io.vertigo.datafactory.collections.model.FacetedQueryResult;
import io.vertigo.datafactory.collections.model.SelectedFacetValues;

public class FacetPromptUtil {

	private FacetPromptUtil() {
		// Util class
	}

	/**
	 * Experimental. Create a prompt for asking the Llm to resolve facets and criteria from existing facets. Return type should be a FacetPromptResult.
	 *
	 * @param asking the user's question
	 * @param facetValues existing facets
	 * @return the prompt to use
	 */
	public static VPrompt createFacetPrompt(final String asking, final FacetedQueryResult<?, ?> facetValues, final Optional<String> additionalInstructions) {
		final var actualFacets = toJson(facetValues);
		final var instructions = new StringBuilder()
				.append("The user is asking for: `").append(asking.replace("`", "'")).append("`\n")
				.append("With the following facets definition : `\n")
				.append(actualFacets).append("\n`\n")
				.append("""
						Instructions :
						- Detect the language of both the user's request and the facet definitions to ensure correct matching.
						- For each facet value, determine if the user is explicitly asking for this. If yes, select this facet value. If 'isMultiSelectable' is false, select at most one facet value.
						- Do not select any facet value if none strictly correspond to the user asking.
						- Do not invent any facet value, use only the ones that are listed in the definition.
						- If the facet is a range facet and the user is asking for someting inside a range, select the range that includes the user request.
						- For a facets, if all values are corresponding, return null for this facet instead of selecting all facets.
						- Put in the String 'criteria' the minimum possible input to reflect user request that is not present in existing facets, for example the request 'last year users' will select 'last year' in the time facet, 'users' for the type facet and set null to the 'criteria' while 'Juan that arrived 1 year and an half ago' will select the range '1 to 2 years' in the time facet, 'users' for the type facet and set 'Juan' in the 'criteria'.
						""");
		additionalInstructions.ifPresent(instructions::append);

		return VPrompt.builder(instructions.toString()).build();
	}

	public static record FacetPromptResult(
			SelectedFacetValues selectedFacetValues, // selected facets
			String criteria // additional textual criteria
	) {
	}

	private static String toJson(final FacetedQueryResult<?, ?> facetedQueryResult) {
		// TODO refacto to not duplicate this code from FacetedQueryResultJsonSerializerV4
		final List<Facet> facets = facetedQueryResult.getFacets();
		final JsonArray jsonFacet = new JsonArray();
		for (final Facet facet : facets) {
			final JsonArray jsonFacetValues = new JsonArray();
			for (final Entry<FacetValue, Long> entry : facet.getFacetValues().entrySet()) {
				if (entry.getValue() > 0) {
					final JsonObject jsonFacetValuesElement = new JsonObject();
					jsonFacetValuesElement.addProperty("code", entry.getKey().code());
					//jsonFacetValuesElement.addProperty("count", entry.getValue()); // not usefull for LLM
					jsonFacetValuesElement.addProperty("label", entry.getKey().label().getDisplay());
					jsonFacetValues.add(jsonFacetValuesElement);
				}
			}
			final JsonObject jsonFacetElement = new JsonObject();
			jsonFacetElement.addProperty("code", facet.getDefinition().getName());
			jsonFacetElement.addProperty("isMultiSelectable", facet.getDefinition().isMultiSelectable());
			jsonFacetElement.addProperty("label", facet.getDefinition().getLabel().getDisplay());
			jsonFacetElement.add("values", jsonFacetValues);
			jsonFacet.add(jsonFacetElement);
		}
		return jsonFacet.toString();
	}

}
