package nl.ou.testar.StateModel.Persistence.OrientDB.Hydrator;

import com.orientechnologies.orient.core.metadata.schema.OType;
import nl.ou.testar.StateModel.AbstractAction;
import nl.ou.testar.StateModel.AbstractState;
import nl.ou.testar.StateModel.Exception.HydrationException;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.EdgeEntity;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.Property;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.TypeConvertor;
import nl.ou.testar.StateModel.Util.HydrationHelper;
import org.fruit.alayer.Tag;
import org.fruit.alayer.TaggableBase;

import java.util.Set;

public class AbstractActionHydrator implements EntityHydrator<EdgeEntity> {

    @Override
    public void hydrate(EdgeEntity target, Object source) throws HydrationException {
        if (!(source instanceof AbstractAction)) {
            throw new HydrationException("Object provided to the abstract action hydrator was not an abstract action");
        }

        // first make sure the identity property is set
        Property identifier = target.getEntityClass().getIdentifier();
        if (identifier == null) {
            throw new HydrationException();
        }

        // because an abstract action might have multiple concrete actions tied to it, it is possible that the target
        // for the same abstract action might me different states.
        // we combine the ids from the source and target with the abstract action id for this purpose
        Property sourceIdentifier = target.getSourceEntity().getEntityClass().getIdentifier();
        String sourceId = (String)target.getSourceEntity().getPropertyValue(sourceIdentifier.getPropertyName()).right();
        Property targetIdentifier = target.getTargetEntity().getEntityClass().getIdentifier();
        String targetId = (String)target.getTargetEntity().getPropertyValue(targetIdentifier.getPropertyName()).right();

        String edgeId = sourceId + "-" + ((AbstractAction) source).getActionId() + "-" + targetId;
        // make sure the java and orientdb property types are compatible
        OType identifierType = TypeConvertor.getInstance().getOrientDBType(edgeId.getClass());
        if (identifierType != identifier.getPropertyType()) {
            throw new HydrationException();
        }
        target.addPropertyValue(identifier.getPropertyName(), identifier.getPropertyType(), edgeId);

        // add the action id
        target.addPropertyValue("actionId", OType.STRING, ((AbstractAction) source).getActionId());

        // loop through the tagged attributes for this state and add them
        TaggableBase attributes = ((AbstractAction) source).getAttributes();
        for (Tag<?> tag :attributes.tags()) {
            // we simply add a property for each tag
            target.addPropertyValue(tag.name(), TypeConvertor.getInstance().getOrientDBType(attributes.get(tag).getClass()), attributes.get(tag));
        }

        // we need to store the concrete action ids that are connected to this abstract action id
        Property concreteActionIds = HydrationHelper.getProperty(target.getEntityClass().getProperties(), "concreteActionIds");
        if (concreteActionIds == null) {
            throw new HydrationException();
        }
        if (!((AbstractAction) source).getConcreteActionIds().isEmpty()) {
            target.addPropertyValue(concreteActionIds.getPropertyName(), concreteActionIds.getPropertyType(), ((AbstractAction) source).getConcreteActionIds());
        }
    }
}
