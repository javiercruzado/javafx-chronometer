/**
 * 
 */
/**
 * @author javier
 *
 */
@XmlJavaTypeAdapters({ @XmlJavaTypeAdapter(value = LocalDateAdapter.class, type = LocalDate.class) })
package application.jaxb;

import java.time.LocalDate;

import javax.xml.bind.annotation.adapters.*;
