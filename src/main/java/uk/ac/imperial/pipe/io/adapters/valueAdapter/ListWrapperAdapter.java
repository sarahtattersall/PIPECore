package uk.ac.imperial.pipe.io.adapters.valueAdapter;

import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import uk.ac.imperial.pipe.io.adapters.modelAdapter.ListWrapper;

/**
 * Used for elements which need a value field in their element
 * E.g.
 * <name>
 * <value>
 * value goes here
 * </value>
 * </name>
 */
public final class ListWrapperAdapter<T> extends XmlAdapter<ListWrapper<T>, List<T>> {
	
	@Override
	public List<T> unmarshal(ListWrapper<T> v) throws Exception {
		return v.getItems();
	}


	@Override
	public ListWrapper<T> marshal(List<T> v) throws Exception {
		return new ListWrapper<T>(v );
	}

//    private static <T> List<T> unmarshal(Unmarshaller unmarshaller,
//            Class<T> clazz, String xmlLocation) throws JAXBException {
//        StreamSource xml = new StreamSource(xmlLocation);
//        Wrapper<T> wrapper = (Wrapper<T>) unmarshaller.unmarshal(xml,
//                Wrapper.class).getValue();
//        return wrapper.getItems();
//    }
// 
//    private static void marshal(Marshaller marshaller, List<?> list, String name)
//            throws JAXBException {
//        QName qName = new QName(name);
//        Wrapper wrapper = new Wrapper(list);
//        JAXBElement<Wrapper> jaxbElement = new JAXBElement<Wrapper>(qName,
//                Wrapper.class, wrapper);
//        marshaller.marshal(jaxbElement, System.out);
//    }
	
	
	
    /**
     *
     * @param adaptedString
     * @return unwrapped string
     */
//    @Override
//    public String unmarshal(AdaptedString adaptedString) {
//        return adaptedString.value;
//    }
//
//    /**
//     *
//     * @param s
//     * @return wrapped string
//     */
//    @Override
//    public AdaptedString marshal(String s) {
//        AdaptedString adaptedString = new AdaptedString();
//        adaptedString.value = s;
//        return adaptedString;
//    }

    /**
     * Wraps the string in a field called value
     */
//    public static class AdaptedString {
//        /**
//         * String element
//         */
//        public String value;
//    }

}
