package de.jochor.rdf.graphviews.databind;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RdfValue {

	private String value;

	private RDFDatatype dataType;

	private String lang;

	public RdfValue(Literal literal) {
		value = literal.getLexicalForm();
		dataType = literal.getDatatype();
		lang = literal.getLanguage();
	}

	public Literal toRDFNode(Model model) {
		Literal literal;
		if (lang == null) {
			literal = model.createTypedLiteral(value, dataType);
		} else {
			literal = model.createLiteral(value, lang);
		}
		return literal;
	}

}
