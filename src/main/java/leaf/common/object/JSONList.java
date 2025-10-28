package leaf.common.object;

import leaf.common.util.Valid;

import java.util.*;

/**
 * JSONList类
 * 实现 List 类
 */
public class JSONList implements List<Object> {
    /**
     * List
     */
    private List<Object> list;

    /**
     * 构造器，用来初始化私有list，默认ArrayList
     */
    public JSONList() { this.list = new ArrayList<>(); }

    /**
     * 构造器，用来初始化私有list
     */
    public JSONList(List<Object> list) { this.list = list; }

    /**
     * id字段名
     */
    private String ID_FIELD_NAME;

    /**
     * 父级id字段名
     */
    private String PARENT_ID_FIELD_NAME;

    /**
     * 子节点字段名
     */
    private String CHILD_FIELD_NAME;

    /**
     * 是否保留id字段
     */
    private boolean IS_RESERVE_ID_FIELD;

    /**
     * 保留父级id字段名
     */
    private boolean IS_RESERVE_FIELD_ID_FIELD;

    /**
     * List转JSONList（递归）
     * @param list 需要转换的List
     * @return 转换后的JSONList类型的值
     */
    public static JSONList toJSONList(List<Object> list) {
        JSONList jsonList = new JSONList();
        JSONMap jsonMapSon;
        JSONList jsonListSon;
        for(Object object : list) {
            if(object.getClass().getName().contains("Map")) {
                jsonMapSon = JSONMap.toJSONMap((Map<String, Object>) object);
                jsonList.add(jsonMapSon);
                continue;
            }
            if(object.getClass().getName().contains("List")) {
                jsonListSon = JSONList.toJSONList((List) object);
                jsonList.add(jsonListSon);
                continue;
            }
            jsonList.add(object);
        }
        return jsonList;
    }

    /**
     * JSONList转List
     * @return 转换后的List类型的值
     */
    public List<Object> toList() { return this.list; }

    /**
     * 获取此列表中指定位置的元素并转成String类型
     * @param index 指定元素的索引
     * @return 指定的元素
     */
    public String getString(int index) {
        Object obj = this.list.get(index);
        if(obj == null) return null;
        return String.valueOf(obj);
    }

    /**
     * 返回此列表中指定位置的元素并转成Map类型
     * @param index 指定元素的索引
     * @return 指定的元素
     */
    public JSONMap getMap(int index) { return (JSONMap)this.list.get(index); }

    /**
     * 返回此列表中指定位置的元素并转成List类型
     * @param index 指定元素的索引
     * @return 指定的元素
     */
    public JSONList getList(int index) { return (JSONList)this.list.get(index); }

    /**
     * 返回此列表中指定位置的元素
     * @param index 指定元素的索引
     * @return 指定的元素
     */
    @Override
    public Object get(int index) {
        return this.list.get(index);
    }

    /**
     * 将指定的元素追加到此列表的末尾
     * @param o 要附加到此列表的元素
     * @return true
     */
    @Override
    public boolean add(Object o) {
        return this.list.add(o);
    }

    /**
     * 在此列表中的指定位置插入指定的元素
     * @param index 插入指定元素的索引
     * @param element 要插入的元素
     */
    @Override
    public void add(int index, Object element) {
        this.list.add(index,element);
    }

    /**
     * 删除该列表中指定位置的元素
     * @param index 删除指定元素的索引
     * @return 先前在指定位置的元素
     */
    @Override
    public Object remove(int index) {
        return this.list.remove(index);
    }

    /**
     * 从列表中删除指定元素的第一个出现（如果存在）
     * @param o 要从此列表中删除的元素（如果存在）
     * @return true如果此列表包含指定的元素
     */
    @Override
    public boolean remove(Object o) {
        return this.list.remove(o);
    }

    /**
     * 按指定集合的Iterator返回的顺序将指定集合中的所有元素追加到此列表的末尾
     * @param c 包含要添加到此列表的元素的集合
     * @return true如果此列表因呼叫而更改
     */
    @Override
    public boolean addAll(Collection<?> c) {
        return this.list.addAll(c);
    }

    /**
     * 按指定集合的Iterator返回的顺序将指定集合中的所有元素追加到此列表的末尾
     * @param index 从中指定集合插入第一个元素的索引
     * @param c 包含要添加到此列表的元素的集合
     * @return true如果此列表因呼叫而更改
     */
    @Override
    public boolean addAll(int index, Collection<?> c) {
        return this.list.addAll(index,c);
    }

    /**
     * 从此列表中删除指定集合中包含的所有元素
     * @param c 包含要从此列表中删除的元素的集合
     * @return true如果此列表因呼叫而更改
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return this.list.remove(c);
    }

    /**
     * 从列表中删除所有元素
     */
    @Override
    public void clear() {
        this.list.clear();
    }

    /**
     * 返回此列表中的元素数s
     * @return 此列表中的元素数量
     */
    @Override
    public int size() {
        return this.list.size();
    }

    /**
     * 如果此列表不包含元素，则返回 true
     * @return true如果此列表不包含任何元素
     */
    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    /**
     * 如果此列表包含指定的元素，则返回 true
     * @param c 包含要保留在此列表中的元素的集合
     * @return true如果此列表因呼叫而更改
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return this.list.retainAll(c);
    }

    /**
     * 如果此列表包含指定的元素，则返回 true
     * @param o 要在此列表中存在的元素要测试的元素
     * @return true如果此列表包含指定的元素
     */
    @Override
    public boolean contains(Object o) {
        return this.list.contains(o);
    }

    /**
     * 以正确的顺序返回该列表中的元素的迭代器
     * @return 以适当的顺序在此列表中的元素上的迭代器
     */
    @Override
    public Iterator<Object> iterator() {
        return this.list.iterator();
    }

    /**
     * 如果此列表包含指定 集合的所有元素，则返回true
     * @param c 要在此列表中检查遏制的集合
     * @return true如果此列表包含指定集合的所有元素
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return this.list.containsAll(c);
    }

    /**
     * 用指定的元素（可选操作）替换此列表中指定位置的元素
     * @param index 要替换的元素的索引
     * @param element 要存储在指定位置的元素
     * @return 先前在指定位置的元素
     */
    @Override
    public Object set(int index, Object element) {
        return this.list.set(index,element);
    }

    /**
     * 返回此列表中指定的 fromIndex （含）和 toIndex之间的视图
     * @param fromIndex fromIndex低端点（含）
     * @param toIndex toIndex高端点（排他）
     * @return 此列表中指定范围的视图
     */
    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        return this.list.subList(fromIndex,toIndex);
    }

    /**
     * 以适当的顺序返回包含此列表中所有元素的数组（从第一个元素到最后一个元素）; 返回数组的运行时类型是指定数组的运行时类型。 如果列表适合指定的数组，则返回其中。 否则，将为新数组分配指定数组的运行时类型和此列表的大小。
     * 如果列表适合指定的数组，并且有空余空间（即，数组的元素多于列表），则紧跟在列表末尾之后的数组中的元素将设置为null 。 （ 仅当调用者知道列表不包含任何null元素时，这在确定列表长度时很有用。）
     * @return 包含此列表元素的数组
     */
    @Override
    public Object[] toArray() {
        return this.list.toArray();
    }

    /**
     * 以适当的顺序返回包含此列表中所有元素的数组（从第一个元素到最后一个元素）; 返回数组的运行时类型是指定数组的运行时类型。 如果列表适合指定的数组，则返回其中。 否则，将为新数组分配指定数组的运行时类型和此列表的大小。
     * 如果列表适合指定的数组，并且有空余空间（即，数组的元素多于列表），则紧跟在列表末尾之后的数组中的元素将设置为null 。 （ 仅当调用者知道列表不包含任何null元素时，这在确定列表长度时很有用。）
     * @param a 要存储此列表元素的数组（如果足够大）; 否则，为此目的分配相同运行时类型的新数组
     * @param <T> 要存储此列表元素的数组（如果足够大）; 否则，为此目的分配相同运行时类型的新数组
     * @return 包含此列表元素的数组
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return this.list.toArray(a);
    }

    /**
     * 返回此列表中第一次出现的指定元素的索引，如果此列表不包含该元素，则返回-1。 更正式的是，返回最低指数i如Objects.equals(o, get(i)) ，如果没有这样的指数则返回-1
     * @param o 要搜索的元素
     * @return 此列表中第一次出现的指定元素的索引，如果此列表不包含该元素，则返回-1
     */
    @Override
    public int indexOf(Object o) {
        return this.list.indexOf(o);
    }

    /**
     * 返回此列表中指定元素最后一次出现的索引，如果此列表不包含该元素，则返回-1。 更正式的是，返回最高指数i如Objects.equals(o, get(i)) ，如果没有这样的指数则为-1
     * @param o 要搜索的元素
     * @return 此列表中指定元素最后一次出现的索引，如果此列表不包含该元素，则返回-1
     */
    @Override
    public int lastIndexOf(Object o) {
        return this.list.lastIndexOf(0);
    }

    /**
     * 返回此列表中元素的列表迭代器（按适当顺序）
     * @return 列表中的元素列表迭代器（按正确顺序）
     */
    @Override
    public ListIterator<Object> listIterator() {
        return this.list.listIterator();
    }

    /**
     * 从列表中的指定位置开始，返回列表中元素的列表迭代器（按正确顺序）。 指定的索引指示初始调用next将返回的第一个元素。 对previous的初始调用将返回指定索引减去1的元素
     * @param index 从列表迭代器返回的第一个元素的索引（通过调用 next ）
     * @return 列表中元素的列表迭代器（按正确顺序），从列表中的指定位置开始
     */
    @Override
    public ListIterator<Object> listIterator(int index) {
        return this.list.listIterator(index);
    }

    /**
     * 将扁平化数组转为树结构（循环） 时间复杂度：O(n^2)；空间复杂度：O(n)
     * @param idFieldName id字段名
     * @param parentIdFieName 父级id字段名
     * @param childFieldName 子节点字段名
     * @return 转换后的树结构
     */
    @Deprecated
    public JSONList listToTree(String idFieldName,String parentIdFieName,String childFieldName) {
        //双层for 主要思想：外层循环-找父节点；内层循环-找子节点；因为每个元素都会找一遍，所有最终得到完整的树
        JSONList result = new JSONList();
        for (Object obj : list) {
            if(!"leaf.common.object.JSONMap".equals(obj.getClass().getName())) {
                result.add(obj);
                continue;
            }
            if(Valid.isEmpty(((JSONMap)obj).getString(parentIdFieName))) {//根节点、顶级节点，直接放入最终返回结果的List
                result.add(obj);
            }
            for (Object objSon : list) { //根据当前parentId找它的子节点
                if(!"leaf.common.object.JSONMap".equals(objSon.getClass().getName())) continue;
                if(((JSONMap)obj).getString(idFieldName).equals(((JSONMap)objSon).getString(parentIdFieName))) {
                    if(((JSONMap)obj).get(childFieldName) == null) { //还没有任何子节点，new一个空的放进去
                        ((JSONMap) obj).put(childFieldName,new JSONList());
                    }
                    ((JSONList)((JSONMap)obj).get(childFieldName)).add(objSon);
                }
            }
        }
        return result;
    }

    /**
     * 将扁平化数组转为树结构（循环） 时间复杂度：O(n^2)；空间复杂度：O(n)
     * @param idFieldName id字段名
     * @param parentIdFieName 父级id字段名
     * @param childFieldName 子节点字段名
     * @param isReserveIdField 是否保留id字段
     * @param isReserveParentIdField 是否保留父级id字段
     * @param notFindParentIsShowTop 没有找到父级是否显示顶层
     * @return 转换后的树结构
     */
    public JSONList listToTree(String idFieldName,String parentIdFieName,String childFieldName,
                                boolean isReserveIdField,boolean isReserveParentIdField,boolean notFindParentIsShowTop) {
        JSONList tree = new JSONList();//返回的map Tree树形结构
        JSONMap nodeMap = new JSONMap();//声明一个map用来存listMap中的对象，key为对象id，value为对象本身

        //循环listMap把map对象put到nodeMap中去
        list.forEach(obj -> {
            if("leaf.common.object.JSONMap".equals(obj.getClass().getName()))
                nodeMap.put(((JSONMap)obj).get(idFieldName).toString(), obj);
        });

        //循环listMap进行Tree树形结构组装
        list.forEach(obj -> {
            if("leaf.common.object.JSONMap".equals(obj.getClass().getName())) {
                //获取map的parentId
                String parentId = ((JSONMap)obj).getString(parentIdFieName);

                if(isReserveIdField) {
                    ((JSONMap)obj).remove(idFieldName);
                }

                if(isReserveParentIdField) {
                    ((JSONMap)obj).remove(parentIdFieName);
                }

                //判断pid是否为空，为空说明是最顶级，直接add到返回的treeMap中去
                if(Valid.isEmpty(parentId)) {
                    tree.add(obj);
                } else {
                    JSONMap parentMap = nodeMap.getMap(parentId);//根据当前map的parentId获取上级parentMap

                    if(parentMap == null) {//如果parentMap为空，则说明当前map没有父级，当前map就是顶级
                        if(notFindParentIsShowTop) {
                            tree.add(obj);
                        }
                    } else {//如果parentMap不为空，则当前map为parentMap的子级
                        //取出parentMap的所有子级的List集合
                        JSONList children = parentMap.getList(childFieldName);
                        //判断子级集合是否为空，为空则新创建List
                        if(children == null) {
                            children = new JSONList();
                            parentMap.put(childFieldName, children);
                        }
                        children.add(obj);//把当前map对象add到parentMap的子级List中去
                    }
                    //因为parentMap是从nodeMap中get出来的，
                    //而nodeMap中的value又是来自于listMap对象，
                    //所以parentMap和nodeMap中的value的地址都是指向listMap中的对象，
                    //所以parentMap的children和entityMap中的value的children改变时，都会改变listMap中的对象。
                }
            } else {
                tree.add(obj);
            }
        });
        return tree;
    }

    /**
     * 将扁平化数组转为树结构（递归）
     * @param idFieldName id字段名
     * @param parentIdFieName 父级id字段名
     * @param childFieldName 子节点字段名
     * @param isReserveIdField 是否保留id字段
     * @param isReserveParentIdField 是否保留父级id字段
     * @return 转换后的树结构
     */
    @Deprecated
    public JSONList listToTreeOld(String idFieldName,String parentIdFieName,String childFieldName,
                               boolean isReserveIdField,boolean isReserveParentIdField) {
        ID_FIELD_NAME = idFieldName;
        PARENT_ID_FIELD_NAME = parentIdFieName;
        CHILD_FIELD_NAME = childFieldName;
        IS_RESERVE_ID_FIELD = isReserveIdField;
        IS_RESERVE_FIELD_ID_FIELD = isReserveParentIdField;
        JSONMap map = new JSONMap();
        JSONMap parentIdToChildrenMap = new JSONMap();//{父级id=[节点集合]}

        //遍历所有列
        for (int i = 0;i < list.size();i++) {
            String id = ((JSONMap)list.get(i)).getString(idFieldName);
            String parentId = ((JSONMap)list.get(i)).getString(parentIdFieName);

            if(id == null) {
                return null;
            }
            if(Valid.isBlank(parentId) || parentId.equals(id)) {
                parentId = null;
            }

            map.put(id, list.get(i));
            parentIdToChildrenMap.putIfAbsent(parentId, new JSONList());
            parentIdToChildrenMap.getList(parentId).add(list.get(i));//将节点放在以父级id为key的map中，根节点的key为null
        }
        System.out.println(parentIdToChildrenMap);

        JSONList result = new JSONList();
        JSONList rootNodes = parentIdToChildrenMap.getList(null);//根节点（没有父节点）的列表

        if (rootNodes != null) {
            for (int i = 0;i < rootNodes.size();i++) {
                createNode(rootNodes.getMap(i), parentIdToChildrenMap);
                result.add(rootNodes.getMap(i));
            }
        }

        return result;
    }

    /**
     * 创建节点
     * @param node 节点
     * @param parentIdToChildrenMap {父级id=[节点集合]}
     */
    private void createNode(JSONMap node, JSONMap parentIdToChildrenMap) {
        String id = node.getString(ID_FIELD_NAME);
        JSONList children = parentIdToChildrenMap.getList(id);

        if(!IS_RESERVE_ID_FIELD) node.remove(ID_FIELD_NAME);
        if(!IS_RESERVE_FIELD_ID_FIELD) node.remove(PARENT_ID_FIELD_NAME);

        if (children != null) {
            JSONList childNodes = new JSONList();
            for (int i = 0;i < children.size();i++) {
                createNode(children.getMap(i), parentIdToChildrenMap);
                childNodes.add(children.getMap(i));
            }
            node.put(CHILD_FIELD_NAME, childNodes);
        }
    }

    /**
     * 转成字符串
     * @return 转换后的字符串
     */
    @Override
    public String toString() {
        String str = "[";
        int i = 0,size = this.list.size();
        for(Object obj:this.list) {
            i++;
            if(i != size) {
                if(obj == null) {
                    str += "null,";
                } else if(obj.getClass().getName().contains("String") || obj.getClass().getName().contains("Char")) {
                    str += "\"" + obj.toString().replace("\"","\\\"") + "\",";
                } else {
                    str += obj+",";
                }
            } else {
                if(obj == null) {
                    str += "null";
                } else if(obj.getClass().getName().contains("String") || obj.getClass().getName().contains("Char")) {
                    str += "\"" + obj.toString().replace("\"","\\\"") + "\"";
                } else {
                    str += obj;
                }
            }
        }
        return str+"]";
    }
}
