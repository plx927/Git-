package com.panlingxiao.spring.databinder;
import com.panlingxiao.spring.validation.editor.PointEditor;
import com.panlingxiao.spring.validation.domain.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.*;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.DataBinder;

import java.beans.*;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by panlingxiao on 2016/5/27.
 */
public class TestDataBinder {


    /**
     * DataBinder����������ݰ󶨵���BeanWrapper
     *
     * The BeanWrapper usually isn��t used by application code directly,
     * but by the DataBinder and the BeanFactory.
     *
     */
    @Test
    public void testBasicBinding(){
        Person person = new Person();
        DataBinder dataBinder = new DataBinder(person);

        //���ö���δ֪�������Ƿ���к��Բ���
        //dataBinder.setIgnoreUnknownFields(false);

        MutablePropertyValues pvs = new MutablePropertyValues();
        String name = "Tom";
        String age = "30";
        pvs.addPropertyValue(new PropertyValue("name", name));
        pvs.addPropertyValue(new PropertyValue("age", age));
        pvs.addPropertyValue("nums[1]", "123");
        pvs.add("aa", "bb");
        dataBinder.bind(pvs);
        Assert.assertEquals(name, person.getName());
        Assert.assertEquals(Integer.parseInt(age),person.getAge());
        Assert.assertEquals(2,person.getNums().size());
        Assert.assertEquals(123, person.getNums().get(1).intValue());
        Assert.assertNull(person.getNums().get(0));
    }


    /**
     * ��Spring3.x�У����еľ���ʵ�ֶ���BeanWrapper�����,��Spring4.2����������AbstractNestablePropertyAccessor
     *
     */
    @Test
    public void testBindingNestedProperty(){
        Customer customer = new Customer();
        BeanWrapperImpl wrapper = new BeanWrapperImpl(customer);
        wrapper.setPropertyValue("firstName", "hello");
        wrapper.setPropertyValue(new PropertyValue("surname", "world"));
        Assert.assertEquals("hello", customer.getFirstName());
        Assert.assertEquals("world", customer.getSurname());

        //�����Զ������������ԣ����������NullValueInNestedPathException
        wrapper.setAutoGrowNestedPaths(true);
        wrapper.setPropertyValue("address.street", "aa");
        wrapper.setPropertyValue("address.code", "123456");
    }

    @Test
    public void testRegisterPropertyEditorToDataBinder(){
        Person person = new Person();
        DataBinder dataBinder = new DataBinder(person);
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("birthday", "2016-05-29");
        dataBinder.bind(pvs);
    }

    /**
     * BeanWrapperĬ��û���ṩPropertyEditor���String��Data��ת��
     * ��������Կ�����BeanWrapper�����ṩ��JavaBean�����ķ��ʲ�����ͬʱ����PropertyEditor�Ĺ����ܡ�
     *
     */
    @Test
    public void testRegisterPropertyEditorToBeanWrapper(){
        BeanWrapperImpl person = new BeanWrapperImpl(new Person());
        person.registerCustomEditor(Date.class, new CustomDateEditor(DateFormat.getDateInstance(), true));
        person.setPropertyValue("birthday", "2016-05-29");

    }

    @Test
    public void testPropertyEditorManager() throws Exception {
        PropertyEditorManager.registerEditor(Date.class,MyCustomerDatePropertyEditor.class);
        BeanWrapper wrapper = new BeanWrapperImpl(new Person());
        PropertyDescriptor pd = wrapper.getPropertyDescriptor("birthday");
        PropertyEditor propertyEditor = PropertyEditorManager.findEditor(pd.getPropertyType());
        propertyEditor.setAsText("2016-06-01");
        pd.getWriteMethod().invoke(wrapper.getWrappedInstance(), propertyEditor.getValue());
        System.out.println(Arrays.asList(PropertyEditorManager.getEditorSearchPath()));
    }

    /**
     * ����PropertyEditorManager�Զ���������
     * Springֻ��ʵ����PropertyEditorManager�Ĳ��һ����еĵڶ����淶��
     * ����û��ʹ��PropertyEditorManager����������Ĳ�������
     */
    @Test
    public void testPropertyEditorManagerAutoSerach(){
        BeanWrapper beanWrapper = new BeanWrapperImpl(new Boo());
        beanWrapper.setPropertyValue("foo", "1;2");
        Boo boo = (Boo) beanWrapper.getWrappedInstance();
        Assert.assertEquals(1, boo.getFoo().getX());
        Assert.assertEquals(2, boo.getFoo().getY());
    }

    /**
     * ��PropertyEditorManager��ע�����Spring����Ч�ġ�
     */
    @Test
    public void testPropertyEditorManagerAutoSearch2(){
        PropertyEditorManager.registerEditor(Point.class, PointEditor.class);
        BeanWrapper wrapper = new BeanWrapperImpl(new Circle());
        wrapper.setPropertyValue("point", "1;2");
    }


    /**
     * �Զ���BeanInfo��Ϣ
     */
    @Test
    public void testCustomBeanInfo() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(Circle.class);
        System.out.println(beanInfo.getClass().getName());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Assert.assertEquals(1,propertyDescriptors.length);
        Circle circle = new Circle();
        propertyDescriptors[0].createPropertyEditor(circle).setAsText("1;2");
        Assert.assertEquals(1, circle.getPoint().getX());
        Assert.assertEquals(2, circle.getPoint().getY());
    }


    @Test
    public void testCustomPropertyEditor(){
        BeanWrapper wrapper = new BeanWrapperImpl(new Circle());
        PropertyDescriptor pd = wrapper.getPropertyDescriptor("point");
        pd.createPropertyEditor(wrapper.getWrappedInstance());

    }


    public static  class MyCustomerDatePropertyEditor extends CustomDateEditor{

        public MyCustomerDatePropertyEditor(){
            this(DateFormat.getDateInstance(), true);
        }

        public MyCustomerDatePropertyEditor(DateFormat dateFormat, boolean allowEmpty) {
            super(dateFormat, allowEmpty);
        }
    }








}
