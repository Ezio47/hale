/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                  01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2010.
 */

package eu.esdihumboldt.goml.oml.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import eu.esdihumboldt.cst.align.ICell;
import eu.esdihumboldt.cst.align.IEntity;
import eu.esdihumboldt.cst.align.ISchema;
import eu.esdihumboldt.cst.align.ICell.RelationType;
import eu.esdihumboldt.cst.align.ext.IParameter;
import eu.esdihumboldt.cst.align.ext.IValueClass;
import eu.esdihumboldt.cst.align.ext.IValueExpression;
import eu.esdihumboldt.cst.rdf.IAbout;
import eu.esdihumboldt.goml.align.Alignment;
import eu.esdihumboldt.goml.align.Cell;
import eu.esdihumboldt.goml.align.Entity;
import eu.esdihumboldt.goml.align.Formalism;
import eu.esdihumboldt.goml.align.Schema;
import eu.esdihumboldt.generated.oml.AlignmentType;
import eu.esdihumboldt.generated.oml.CellType;
import eu.esdihumboldt.generated.oml.ClassConditionType;
import eu.esdihumboldt.generated.oml.ClassType;
import eu.esdihumboldt.generated.oml.ComparatorEnumType;
import eu.esdihumboldt.generated.oml.DomainRestrictionType;
import eu.esdihumboldt.generated.oml.EntityType;
import eu.esdihumboldt.generated.oml.FormalismType;
import eu.esdihumboldt.generated.oml.FunctionType;
import eu.esdihumboldt.generated.oml.OntologyType;
import eu.esdihumboldt.generated.oml.ParamType;
import eu.esdihumboldt.generated.oml.PropertyCollectionType;
import eu.esdihumboldt.generated.oml.PropertyCompositionType;
import eu.esdihumboldt.generated.oml.PropertyType;
import eu.esdihumboldt.generated.oml.RangeRestrictionType;
import eu.esdihumboldt.generated.oml.RelationEnumType;
import eu.esdihumboldt.generated.oml.RestrictionType;
import eu.esdihumboldt.generated.oml.ValueClassType;
import eu.esdihumboldt.generated.oml.ValueConditionType;
import eu.esdihumboldt.generated.oml.ValueExprType;
import eu.esdihumboldt.generated.oml.AlignmentType.Map;
import eu.esdihumboldt.generated.oml.PropertyCollectionType.Item;
import eu.esdihumboldt.goml.oml.ext.Parameter;
import eu.esdihumboldt.goml.oml.ext.Transformation;
import eu.esdihumboldt.goml.oml.ext.ValueClass;
import eu.esdihumboldt.goml.oml.ext.ValueExpression;
import eu.esdihumboldt.goml.omwg.ComparatorType;
import eu.esdihumboldt.goml.omwg.ComposedProperty;
import eu.esdihumboldt.goml.omwg.FeatureClass;
import eu.esdihumboldt.goml.omwg.Property;
import eu.esdihumboldt.goml.omwg.Relation;
import eu.esdihumboldt.goml.omwg.Restriction;
import eu.esdihumboldt.goml.omwg.ComposedProperty.PropertyOperatorType;
import eu.esdihumboldt.goml.rdf.About;
import eu.esdihumboldt.goml.rdf.Resource;

/**
 * This class reads the OML Rdf Document into Java Object.
 * 
 * @author Thorsten Reitz
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @version $Id$
 */
public class OmlRdfReader {
	/**
	 * Constant defines the path to the alignment jaxb context
	 */
	private static final String ALIGNMENT_CONTEXT = "eu.esdihumboldt.generated.oml";

	/**
	 * Unmarshalls oml-mapping to the HUMBOLDT Alignment.
	 * 
	 * @param rdfFile
	 *            path to the oml-mapping file
	 * @return Alignment object
	 */
	public Alignment read(String rdfFile) {
		// 1. unmarshal rdf
		JAXBContext jc;
		JAXBElement<AlignmentType> root = null;
		try {
			jc = JAXBContext.newInstance(ALIGNMENT_CONTEXT);
			Unmarshaller u = jc.createUnmarshaller();

			// it will debug problems while unmarshalling
			u
					.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
			/*root = u.unmarshal(new StreamSource(new URL(rdfFile).openStream()),
					AlignmentType.class);*/
			
			root =  u.unmarshal(new StreamSource(new File(rdfFile)),AlignmentType.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AlignmentType genAlignment = root.getValue();

		// 2. create humboldt alignment object and fulfill the required fields
		Alignment al = new Alignment();
		// set about
		al.setAbout(new About(UUID.randomUUID()));
		// set level
		al.setLevel(genAlignment.getLevel());
		// set map with cells
		al.setMap(getMap(genAlignment.getMap()));
		// set schema1,2 containing information about ontologies1,2
		al.setSchema1(getSchema(genAlignment.getOnto1().getOntology()));
		al.setSchema2(getSchema(genAlignment.getOnto2().getOntology()));
		// set Value Class
		al.setValueClass(getValueClass(genAlignment.getValueClass()));
		return al;
	}

	public Alignment read(File rdfFile) {
		Alignment al = null;
		try {
			al = this.read(rdfFile.getCanonicalFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return al;
	}

	/**
	 * Converts from List of JAXB Value Class objects to the list of the OML
	 * objects
	 * 
	 * @param valueClass
	 * @return
	 */

	private List<IValueClass> getValueClass(List<ValueClassType> valueClass) {
		if (valueClass.size()>0){
		List<IValueClass> oValueClasses = new ArrayList<IValueClass>();
		IValueClass oValueClass = new ValueClass();
		Iterator<ValueClassType> iterator = valueClass.iterator();
		ValueClassType vcType;
		while (iterator.hasNext()) {
			vcType = iterator.next();
			// set about
			((ValueClass) oValueClass).setAbout(vcType.getAbout());
			// set resource
			((ValueClass) oValueClass).setResource(vcType.getResource());
			// setValueExpression
			((ValueClass) oValueClass).setValue(getValueExpression(vcType
					.getValue()));
			oValueClasses.add(oValueClass);
		}
		return oValueClasses;
		}
		return null;
	}

	/**
	 * converts from JAXB Ontology {@link OntologyType} to OML schema
	 * {@link ISchema}
	 * 
	 * @param onto
	 *            Ontology
	 * @return schema
	 */
	private ISchema getSchema(OntologyType onto) {
		// creates formalism
		// create Formalism
		Formalism formalism = getFormalism(onto.getFormalism());
		ISchema schema = new Schema(onto.getLocation(), formalism);
		// set about
		IAbout about = new About(UUID.randomUUID());
		((About) about).setAbout(onto.getAbout());
		((Schema) schema).setAbout(about);
		// set labels
		((Schema) schema).getLabels().addAll(onto.getLabel());

		return schema;

	}

	/**
	 * converts from JAXB FromalismType {@link FormalismType} to OML
	 * {@link Formalism}
	 * 
	 * @param jaxbFormalism
	 * @return Formalism
	 */
	private Formalism getFormalism(
			eu.esdihumboldt.generated.oml.OntologyType.Formalism jaxbFormalism) {
		Formalism formalism = null;
		if (jaxbFormalism != null) {
			FormalismType fType = jaxbFormalism.getFormalism();
			URI uri = null;
			try {
				uri = new URI(fType.getUri());
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
			formalism = new Formalism(fType.getName(), uri);
		}
		return formalism;
	}

	/**
	 * 
	 * @param map
	 *            List of the generated maps, containing cell{@link CellType}
	 *            and String about
	 * @return List of Cells {@link Cell}
	 */
	private List<ICell> getMap(List<Map> maps) {
		List<ICell> cells = new ArrayList<ICell>(maps.size());
		Cell cell;
		Map map;
		Iterator<Map> iterator = maps.iterator();
		while (iterator.hasNext()) {
			map = iterator.next();
			cell = getCell(map.getCell());
			cells.add(cell);

		}
		return cells;
	}

	/**
	 * converts from {@link CellType} to {@link Cell}
	 * 
	 * @param cell
	 * @return
	 */
	private Cell getCell(CellType cellType) {
		Cell cell = new Cell();

		List<String> labels = cellType.getLabel();
		cell.setLabel(labels);

		// TODO check with Marian set about as UUID from string about
		// cell.setAbout(new About(UUID.fromString(cellType.getAbout())));
		About about = new About(UUID.randomUUID());
		about.setAbout(cellType.getAbout());
		cell.setAbout(about);
		// set entity1
		cell.setEntity1(getEntity(cellType.getEntity1().getEntity()));
		// set entity2
		cell.setEntity2(getEntity(cellType.getEntity2().getEntity()));
		// Measure is optional
		if (cellType.getMeasure() != null)
			cell.setMeasure(cellType.getMeasure());
		// Relation is optional
		if (cellType.getRelation() != null)
			cell.setRelation(getRelation(cellType.getRelation()));

		return cell;
	}

	/**
	 * converts from RelationEnumType to RelationType
	 * 
	 * @param relation
	 * @return
	 */
	private RelationType getRelation(RelationEnumType relation) {

		if (relation.equals(RelationEnumType.DISJOINT)) {
			return RelationType.Disjoint;
		} else if (relation.equals(RelationEnumType.EQUIVALENCE)) {
			return RelationType.Equivalence;
		} else if (relation.equals(RelationEnumType.EXTRA)) {
			return RelationType.Extra;
		} else if (relation.equals(RelationEnumType.HAS_INSTANCE)) {
			return RelationType.HasInstance;
		} else if (relation.equals(RelationEnumType.INSTANCE_OF)) {
			return RelationType.InstanceOf;
		} else if (relation.equals(RelationEnumType.MISSING)) {
			return RelationType.Missing;
		} else if (relation.equals(RelationEnumType.PART_OF)) {
			return RelationType.PartOf;
		} else if (relation.equals(RelationEnumType.SUBSUMED_BY)) {
			return RelationType.SubsumedBy;
		} else if (relation.equals(RelationEnumType.SUBSUMES)) {
			return RelationType.Subsumes;
		}
		return null;
	}

	/**
	 * Converts from the JAXB generated EntityType to the {@link IEntity}
	 * 
	 * @param entity
	 * @return
	 */
	private IEntity getEntity(JAXBElement<? extends EntityType> jaxbEntity) {
		EntityType entityType = jaxbEntity.getValue();
		// TODO allow to instantiate entity as Property, FeatureClass, Relation,
		// ComposedFeatureClass, ComposedFeatureType, composedRelation
		// instantiate entity es property

		Entity entity = null;

		// TODO add convertion to the RelationType if needed
		if (entityType instanceof PropertyType) {

			// make decision whether it is ComposedProperty
			if (((PropertyType) entityType).getPropertyComposition() != null) {
				PropertyCompositionType propCompType = ((PropertyType) entityType)
						.getPropertyComposition();
				// instantiate entity as ComposedProperty
				IAbout about = new About(entityType.getAbout());
				entity = new ComposedProperty(getOperator(propCompType
						.getOperator()), about);

				// 2. set collection of properties, a single property or
				// relation

				if (propCompType.getCollection().getItem().size() > 0) {
					// set collection
					((ComposedProperty) entity)
							.setCollection(getPropertyCollection(propCompType
									.getCollection()));
				} else if (propCompType.getProperty() != null) {
					// set Property
					((ComposedProperty) entity).getCollection().add(
							getProperty(propCompType.getProperty()));
				} else if (propCompType.getRelation() != null) {
					// set Relation
					((ComposedProperty) entity)
							.setRelation(getOMLRelation(propCompType
									.getRelation()));
				}
				// set ComposedProperty specific members
			} else {
				// instantiate entity as property
				entity = new Property(new About(entityType.getAbout()));
			}
			// set property-specific members to the entity
			PropertyType propertyType = ((PropertyType) entityType);
			// set domainRestriction
			((Property) entity)
					.setDomainRestriction(getDomainRestriction(propertyType
							.getDomainRestriction()));
			// set typeCondition
			((Property) entity).setTypeCondition(propertyType
					.getTypeCondition());
			// set value conditions
			((Property) entity)
					.setValueCondition(getValueCondition(propertyType
							.getValueCondition()));

		} else if (entityType instanceof ClassType) {
			// initiates entity as FeatureType
			ClassType cType = (ClassType) entityType;
			entity = new FeatureClass(new About(entityType.getAbout()));
			((FeatureClass) entity)
					.setAttributeOccurenceCondition(getRestrictions(cType
							.getAttributeOccurenceCondition()));
			((FeatureClass) entity)
					.setAttributeTypeCondition(getRestrictions(cType
							.getAttributeTypeCondition()));
			((FeatureClass) entity)
					.setAttributeValueCondition(getRestrictions(cType
							.getAttributeValueCondition()));
		}
		if (entityType.getTransf() != null) {
			// set Transformation to Entity
			Transformation transformation = getTransformation(entityType
					.getTransf());
			entity.setTransformation(transformation);
		}
		// set About
		About about = new About(UUID.randomUUID());
		about.setAbout(entityType.getAbout());
		entity.setAbout(about);
		// set Labels
		entity.setLabel(entityType.getLabel());
		return entity;
	}

	/**
	 * Implements casting from the JAXB-Type to the OML Type for the Relation
	 * element.
	 * 
	 * 
	 * @param relation
	 *            jaxb-generated object
	 * @return omlRelation
	 */
	private Relation getRelation(
			eu.esdihumboldt.generated.oml.RelationType relation) {
		Relation omlRelation = new Relation(new About(""));
		if (relation != null) {
			// set about
			if (relation.getAbout() != null && !relation.getAbout().equals("")) {
				omlRelation.setAbout(new About(relation.getAbout()));
			}
			// set domain restriction
			if (relation.getDomainRestriction() != null) {
				FeatureClass domainRestriction = getDomainRestriction(relation
						.getDomainRestriction());
				ArrayList<FeatureClass> domainRestrictions = new ArrayList<FeatureClass>();
				domainRestrictions.add(domainRestriction);
				omlRelation.setDomainRestriction(domainRestrictions);
			}
			// set label
			if (relation.getLabel() != null && relation.getLabel().size() > 0) {
				omlRelation.setLabel(relation.getLabel());

			}
			// set transformation
			if (relation.getTransf() != null) {
				omlRelation.setTransformation(getTransformation(relation
						.getTransf()));
			}
			// set Range Restriction
			if (relation.getRangeRestriction() != null) {
				FeatureClass omlRangeRestriction = getRangeRestriction(relation
						.getRangeRestriction());
				List<FeatureClass> omlRangeRestrictions = new ArrayList<FeatureClass>();
				omlRangeRestrictions.add(omlRangeRestriction);
				omlRelation.setRangeRestriction(omlRangeRestrictions);
			}

		}
		return omlRelation;
	}

	/**
	 * Implements casting from the JAXB-Type to the OML Type for the Property
	 * element.
	 * 
	 * 
	 * @param property
	 *            jaxb-generated object
	 * @return omlProperty
	 */
	private Property getProperty(PropertyType property) {
		Property omlProperty;
		//make decision: simple property or composed property
		if (property.getPropertyComposition()!=null){
			PropertyCompositionType pcType = property.getPropertyComposition();
			//initialize as composed property
			omlProperty = new ComposedProperty(getOperator(pcType.getOperator()),new About(""));
			//set collection of properties and/or relation
			if (pcType.getCollection() != null && pcType.getCollection().getItem()!= null){
				//set collection
				((ComposedProperty)omlProperty).setCollection(getPropertyCollection(pcType.getCollection()));
			}else if (pcType.getProperty() != null){
				//set a single property
				((ComposedProperty)omlProperty).getCollection().add(getProperty(pcType.getProperty()));
			}
			if (pcType.getRelation() != null){
				//set relation
				((ComposedProperty)omlProperty).setRelation(getRelation(pcType.getRelation()));
			}
			
		}else{// initialize as property
		 omlProperty = new Property(new About(""));
		}
		if (property != null) {
			// set About
			if (property.getAbout() != null && !property.getAbout().equals("")) {
				omlProperty.setAbout(new About(property.getAbout()));
			}
			// set domain restriction
			if (property.getDomainRestriction() != null) {
				omlProperty.setDomainRestriction(getDomainRestriction(property
						.getDomainRestriction()));
			}
			// set label
			if (property.getLabel() != null) {
				omlProperty.setLabel(property.getLabel());
			}

			// set transformation
			if (property.getTransf() != null) {
				omlProperty.setTransformation(getTransformation(property
						.getTransf()));
			}
			// set type condition
			if (property.getTypeCondition() != null) {
				omlProperty.setTypeCondition(property.getTypeCondition());
			}
			// set value condition
			if (property.getValueCondition() != null) {
				omlProperty.setValueCondition(getValueCondition(property
						.getValueCondition()));
			}

		}

		return omlProperty;
	}

	/**
	 * Returns a List of OML Properties for given jaxb-based
	 * PropertyCollectionType.
	 * 
	 * @param collection
	 * @return
	 */
	private List<Property> getPropertyCollection(
			PropertyCollectionType collection) {
		List<Property> properties = null;
		if (collection != null) {
			properties = new ArrayList<Property>();
			List<Item> collectionItems = collection.getItem();
			Iterator<Item> iterator = collectionItems.iterator();
			Item item;
			Property property;
			while (iterator.hasNext()) {
				item = iterator.next();
				property = getProperty(item.getProperty());
				properties.add(property);
			}
		}
		return properties;
	}

	/**
	 * Converts from the JAXB-Based RelationType to the OML Relation
	 * 
	 * @param relation
	 * @return
	 */
	private Relation getOMLRelation(
			eu.esdihumboldt.generated.oml.RelationType relationType) {
		Relation relation = null;
		if (relationType != null) {
			IAbout about = new About(relationType.getAbout());
			relation = new Relation(about);
			// set label list
			relation.setLabel(relationType.getLabel());
			// set transformation
			relation.setTransformation(getTransformation(relationType
					.getTransf()));
			// set Range Restriction
			List<FeatureClass> features = new ArrayList<FeatureClass>();
			features
					.add(getRangeRestriction(relationType.getRangeRestriction()));
			relation.setRangeRestriction(features);
			// set Domain Restriction
			features = new ArrayList<FeatureClass>();
			features.add(getDomainRestriction(relationType
					.getDomainRestriction()));
			relation.setDomainRestriction(features);
		}
		return relation;
	}

	/**
	 * Converts from the JAXB-based DomainRestrictionType to the OML
	 * FeatureClass
	 * 
	 * @param domainRestriction
	 * @return
	 */
	private FeatureClass getDomainRestriction(
			DomainRestrictionType domainRestriction) {
		FeatureClass fClass;
		ClassType clazz;

		fClass = new FeatureClass(null);
		if (domainRestriction != null) {
			clazz = domainRestriction.getClazz();
			if (clazz != null) {
				// set about
				About fAbout = new About(java.util.UUID.randomUUID());
				fAbout.setAbout(clazz.getAbout());
				fClass.setAbout(fAbout);
				// set attributeValueCondition list
				fClass.setAttributeValueCondition(getRestrictions(clazz
						.getAttributeValueCondition()));
				// set attributeTypeCondition list
				fClass.setAttributeTypeCondition(getRestrictions(clazz
						.getAttributeTypeCondition()));
				// set attributeOccurenceCondition
				fClass.setAttributeOccurenceCondition(getRestrictions(clazz
						.getAttributeOccurenceCondition()));
				// set label list
				fClass.setLabel(clazz.getLabel());
				// set transformation
				fClass.setTransformation(getTransformation(clazz.getTransf()));
			}
		}

		return fClass;
	}

	/**
	 * Converts from the JAXB-based RangeRestrictionType to the OML FeatureClass
	 * 
	 * @param domainRestriction
	 * @return
	 */
	private FeatureClass getRangeRestriction(
			RangeRestrictionType rangeRestriction) {
		FeatureClass fClass;
		ClassType clazz;

		fClass = new FeatureClass(null);
		if (rangeRestriction != null) {
			clazz = rangeRestriction.getClazz();
			if (clazz != null) {
				// set about
				About fAbout = new About(java.util.UUID.randomUUID());
				fAbout.setAbout(clazz.getAbout());
				fClass.setAbout(fAbout);
				// set attributeValueCondition list
				fClass.setAttributeValueCondition(getRestrictions(clazz
						.getAttributeValueCondition()));
				// set attributeTypeCondition list
				fClass.setAttributeTypeCondition(getRestrictions(clazz
						.getAttributeTypeCondition()));
				// set attributeOccurenceCondition
				fClass.setAttributeOccurenceCondition(getRestrictions(clazz
						.getAttributeOccurenceCondition()));
				// set label list
				fClass.setLabel(clazz.getLabel());
				// set transformation
				fClass.setTransformation(getTransformation(clazz.getTransf()));
			}
		}

		return fClass;
	}

	/**
	 * Creates s list of properties from the jaxb-based PropertyCollectionType
	 * 
	 * @param collection
	 * @return
	 *//*
	private List<Property> getPropertyCollecion(
			PropertyCollectionType collection) {
		List<Property> properties = new ArrayList<Property>();
		Property property;
		PropertyType propType;
		if (collection != null) {
			List<Item> items = collection.getItem();
			if (items != null) {
				Iterator<?> iterator = items.iterator();
				while (iterator.hasNext()) {
					propType = ((Item) iterator.next()).getProperty();
					property = getSimpleProperty(propType);
					properties.add(property);
				}

			}
		}
		return properties;
	}

	*//**
	 * Converts the instance of the JAXB-based property type to the simple/non
	 * composed property
	 * 
	 * @param propType
	 * @return
	 *//*
	private Property getSimpleProperty(PropertyType propType) {
		Property property = null;
		if (propType != null) {
			IAbout about = new About(propType.getAbout());
			property = new Property(about);
			// set domainRestriction
			property.setDomainRestriction(getDomainRestriction(propType
					.getDomainRestriction()));
			// set typeCondition
			property.setTypeCondition(propType.getTypeCondition());
			// set value conditions
			property.setValueCondition(getValueCondition(propType
					.getValueCondition()));
			// set transformation
			Transformation transformation = getTransformation(propType
					.getTransf());

			property.setTransformation(transformation);
			// set labels
			property.setLabel(propType.getLabel());
		}

		return property;
	}*/

	/**
	 * Converts propertyOperator instance from the JAXB-based enum to the OML
	 * enum
	 * 
	 * @param propertyOperatorType
	 * @return
	 */
	private PropertyOperatorType getOperator(
			eu.esdihumboldt.generated.oml.PropertyOperatorType operator) {
		if (operator != null) {
			if (operator != null) {
				if (operator
						.equals(eu.esdihumboldt.generated.oml.PropertyOperatorType.INTERSECTION))
					return eu.esdihumboldt.goml.omwg.ComposedProperty.PropertyOperatorType.AND;
				if (operator
						.equals(eu.esdihumboldt.generated.oml.PropertyOperatorType.UNION))
					return eu.esdihumboldt.goml.omwg.ComposedProperty.PropertyOperatorType.OR;
				if (operator
						.equals(eu.esdihumboldt.generated.oml.PropertyOperatorType.FIRST))
					return eu.esdihumboldt.goml.omwg.ComposedProperty.PropertyOperatorType.FIRST;
				if (operator
						.equals(eu.esdihumboldt.generated.oml.PropertyOperatorType.NEXT))
					return eu.esdihumboldt.goml.omwg.ComposedProperty.PropertyOperatorType.NEXT;

			}

		}
		return null;
	}

	/**
	 * Converts from the FunctionType to the Transformation
	 * 
	 * @param transf
	 * @return
	 */
	private Transformation getTransformation(FunctionType transf) {
		Transformation trans = new Transformation();
		if (transf != null) {
			// set Service
			if (transf.getResource() != null) {
				Resource resource = new Resource(transf.getResource());
				trans.setService(resource);
				// set parameter list
				trans.setParameters(getParameters(transf.getParam()));
			} else {
				trans = null;
			}
		}
		return trans;
	}

	private List<IParameter> getParameters(List<ParamType> param) {
		List<IParameter> params = new ArrayList<IParameter>(param.size());
		Iterator<ParamType> iterator = param.iterator();
		ParamType paramType;
		IParameter parameter;
		while (iterator.hasNext()) {
			paramType = iterator.next();
			parameter = new Parameter(paramType.getName(), paramType.getValue()
					.get(0));
			params.add(parameter);
		}
		return params;
	}

	/**
	 * Converts from the List of DomainRestrictionType to the List of the
	 * FeatureClass
	 * 
	 * @param domainRestriction
	 * @return
	 */
	private List<FeatureClass> getDomainRestriction(
	// TODO discuss the propertytype-field
			List<DomainRestrictionType> domainRestriction) {
		List<FeatureClass> classes = new ArrayList<FeatureClass>(
				domainRestriction.size());
		DomainRestrictionType restriction = null;
		FeatureClass fClass;
		ClassType clazz;
		Iterator<DomainRestrictionType> iterator = domainRestriction.iterator();
		while (iterator.hasNext()) {
			restriction = iterator.next();
			clazz = restriction.getClazz();
			fClass = new FeatureClass(null);
			// set about
			About fAbout = new About(java.util.UUID.randomUUID());
			fAbout.setAbout(restriction.getClazz().getAbout());
			fClass.setAbout(fAbout);
			// set attributeValueCondition list
			fClass.setAttributeValueCondition(getRestrictions(clazz
					.getAttributeValueCondition()));
			// set attributeTypeCondition list
			fClass.setAttributeTypeCondition(getRestrictions(clazz
					.getAttributeTypeCondition()));
			// set attributeOccurenceCondition
			fClass.setAttributeOccurenceCondition(getRestrictions(clazz
					.getAttributeOccurenceCondition()));
			classes.add(fClass);
		}
		return classes;
	}

	/**
	 * converts from a list of the ClassConditionType to the list of the
	 * Restriction type
	 * 
	 * @param List
	 *            of ClassConditionType
	 * @return
	 */
	private List<Restriction> getRestrictions(
			List<ClassConditionType> classConditions) {
		List<Restriction> restrictions = new ArrayList<Restriction>(
				classConditions.size());
		Iterator<ClassConditionType> iterator = classConditions.iterator();
		Restriction restriction;
		ClassConditionType classCondition;
		while (iterator.hasNext()) {
			classCondition = iterator.next();
			RestrictionType rType = classCondition.getRestriction();
			List<ValueExprType> valueExpr = rType.getValue();
			// TODO clear with MdV
			// restriction = new Restriction(null,
			// getValueExpression(valueExpr));
			restriction = new Restriction(getValueExpression(valueExpr));
			// set value class to add about and resource document
			ValueClass vClass = new ValueClass();
			ValueClassType vcType = rType.getValueClass();
			if (vcType != null) {
				vClass.setAbout(vcType.getAbout());
				vClass.setResource(vcType.getResource());
				vClass.getValue().addAll(getValueExpression(vcType.getValue()));
				restriction.setValueClass(vClass);
			}

			
			if (rType.getComparator() != null)
				restriction.setComparator(getComparator(rType.getComparator()));
			restriction.setCqlStr(rType.getCqlStr());
			restrictions.add(restriction);

		}

		return restrictions;
	}

	/**
	 * Converts from List<ValueConditionType> to List<Restriction>
	 * 
	 * @param valueCondition
	 * @return
	 */
	private List<Restriction> getValueCondition(
			List<ValueConditionType> valueCondition) {

		List<Restriction> restrictions = new ArrayList<Restriction>(
				valueCondition.size());
		Iterator<ValueConditionType> iterator = valueCondition.iterator();
		Restriction restriction;
		while (iterator.hasNext()) {
			ValueConditionType condition = iterator.next();
			// get List<ValueExpressionType>
			List<ValueExprType> valueExpr = condition.getRestriction()
					.getValue();
			if ((valueExpr == null || valueExpr.size() == 0)
					&& condition.getRestriction().getValueClass() != null) {
				valueExpr = condition.getRestriction().getValueClass()
						.getValue();
			}
			// TODO:clear with MdV
			// restriction = new Restriction(null,
			// getValueExpression(valueExpr));
			restriction = new Restriction(getValueExpression(valueExpr));
			restriction.setComparator(getComparator(condition.getRestriction()
					.getComparator()));
			// add Sequence ID if it exists
			if (condition.getSeq() != null) {
				restriction.setSeq(condition.getSeq());
			}
			// TODO add Property onAttribute
			restrictions.add(restriction);
		}

		return restrictions;
	}

	/**
	 * converts from the ComparatorEnumType to ComparatorType
	 * 
	 * @param comparator
	 * @return
	 */
	private ComparatorType getComparator(ComparatorEnumType comparator) {

		if (comparator.equals(ComparatorEnumType.BETWEEN))
			return ComparatorType.BETWEEN;
		else if (comparator.equals(ComparatorEnumType.COLLECTION_CONTAINS))
			return ComparatorType.COLLECTION_CONTAINS;
		else if (comparator.equals(ComparatorEnumType.CONTAINS))
			return ComparatorType.CONTAINS;
		else if (comparator.equals(ComparatorEnumType.EMPTY))
			return ComparatorType.EMPTY;
		else if (comparator.equals(ComparatorEnumType.ENDS_WITH))
			return ComparatorType.ENDS_WITH;
		else if (comparator.equals(ComparatorEnumType.EQUAL))
			return ComparatorType.EQUAL;
		else if (comparator.equals(ComparatorEnumType.GREATER_THAN))
			return ComparatorType.GREATER_THAN;
		else if (comparator.equals(ComparatorEnumType.GREATER_THAN_OR_EQUAL))
			return ComparatorType.GREATER_THAN_OR_EQUAL;
		else if (comparator.equals(ComparatorEnumType.INCLUDES))
			return ComparatorType.INCLUDES;
		else if (comparator.equals(ComparatorEnumType.INCLUDES_STRICTLY))
			return ComparatorType.INCLUDES_STRICTLY;
		else if (comparator.equals(ComparatorEnumType.LESS_THAN))
			return ComparatorType.LESS_THAN;
		else if (comparator.equals(ComparatorEnumType.LESS_THAN_OR_EQUAL))
			return ComparatorType.GREATER_THAN_OR_EQUAL;
		else if (comparator.equals(ComparatorEnumType.MATCHES))
			return ComparatorType.MATCHES;
		else if (comparator.equals(ComparatorEnumType.NOT_EQUAL))
			return ComparatorType.NOT_EQUAL;
		else if (comparator.equals(ComparatorEnumType.ONE_OF))
			return ComparatorType.ONE_OF;
		else if (comparator.equals(ComparatorEnumType.STARTS_WITH))
			return ComparatorType.STARTS_WITH;
		// TODO clear about otherwise-type
		return null;
	}

	/**
	 * Conversts from the list of <ValueExprType> to the list of ValueExpression
	 * 
	 * @param valueExpr
	 * @return
	 */
	private List<IValueExpression> getValueExpression(
			List<ValueExprType> valueExpr) {
		List<IValueExpression> omlExpressions = new ArrayList<IValueExpression>(
				valueExpr.size());
		ValueExpression omlExpr;
		Iterator<ValueExprType> iterator = valueExpr.iterator();
		while (iterator.hasNext()) {
			ValueExprType jaxbExpr = iterator.next();
			omlExpr = new ValueExpression(jaxbExpr.getLiteral());
			omlExpr.setMax(jaxbExpr.getMax());
			omlExpr.setMin(jaxbExpr.getMin());
			// TODO implement set Apply
			// omlExpr.setApply(getFunction(jaxbExpr.getApply()));
			omlExpressions.add(omlExpr);

		}

		return omlExpressions;
	}
}
