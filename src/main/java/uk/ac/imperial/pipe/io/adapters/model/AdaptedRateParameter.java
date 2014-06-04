package uk.ac.imperial.pipe.io.adapters.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Adapted rate parameter
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class AdaptedRateParameter {

    /**
     * Rate parameter id
     */
    @XmlAttribute
    private String name;

    /**
     * Rate parameter functional expression
     */
    @XmlAttribute
    private String expression;

    /**
     * Rate parameter id
     */
    @XmlAttribute
    private String id;

    /**
     * Rate parameter type, defaults to "real"
     */
    @XmlAttribute
    private String defType = "real";

    /**
     * Rate parameter text type
     */
    @XmlAttribute
    private String type = "text";

    /**
     *
     *  @return text type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type text type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return id
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name id
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return functional expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     *
     * @param expression functional expression that adheres to the rate grammar
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id unique rate id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return def type
     */
    public String getDefType() {
        return defType;
    }

    /**
     *
     * @param defType def type
     */
    public void setDefType(String defType) {
        this.defType = defType;
    }

}
