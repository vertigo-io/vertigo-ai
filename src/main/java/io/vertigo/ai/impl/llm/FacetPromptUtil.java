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
		final var instructions = new StringBuilder("We have a faceted research with the following facets and values: `\n")
				.append(actualFacets).append("\n`\n")
				.append("The user is asking for: `").append(asking.replace("`", "'")).append("`\n")
				.append("Select according facets, user may ask in a different language but select the facet anyway.\n")
				.append("Do not invent any facet code, only use existing ones that are described.\n")
				.append("For a facets, if all values are corresponding, return null for this facet instead of selecting all facets.\n")
				.append("Put in the String 'criteria' the minimum possible input to reflect user request that is not present in existing facets, for example the request 'last year users' will select 'last year' in the time facet, 'users' for the type facet and set null to the 'criteria' while 'Juan that arrived 1 year and an half ago' will select the range '1 to 2 years' in the time facet, 'users' for the type facet and set 'Juan' in the 'criteria'. Double check that the criteria does not duplicate facet value even if it is a traduction of another language or the terms are not exactly the sames but refers to the same intent.\n");
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
					jsonFacetValuesElement.addProperty("count", entry.getValue());
					jsonFacetValuesElement.addProperty("label", entry.getKey().label().getDisplay());
					jsonFacetValues.add(jsonFacetValuesElement);
				}
			}
			final JsonObject jsonFacetElement = new JsonObject();
			jsonFacetElement.addProperty("code", facet.getDefinition().getName());
			jsonFacetElement.addProperty("label", facet.getDefinition().getLabel().getDisplay());
			jsonFacetElement.add("values", jsonFacetValues);
			jsonFacet.add(jsonFacetElement);
		}
		return jsonFacet.toString();
	}

}
