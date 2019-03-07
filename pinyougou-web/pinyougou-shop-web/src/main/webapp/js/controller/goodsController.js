/** 定义控制器层 */
app.controller('goodsController', function($scope, $controller, baseService){

    /** 指定继承baseController */
    $controller('baseController',{$scope:$scope});

    /** 添加商品 */
    $scope.saveOrUpdate = function(){

        // 获取富文本编辑中的内容
        $scope.goods.goodsDesc.introduction = editor.html();

        /** 发送post请求 */
        baseService.sendPost("/goods/save", $scope.goods)
            .then(function(response){
                if (response.data){ // true
                    /** 清空表单中的数据 */
                    $scope.goods = {};

                    /** 清空富文本编辑中的内容 */
                    editor.html("");
                }else{
                    alert("添加失败！");
                }
            });
    };

    // 定义数据存储结构
    $scope.goods = {goodsDesc : {itemImages : [], specificationItems : []}};
    //错误的 $scope.goods.goodsDesc.itemImages = [];

    // 定义文件上传的方法
    $scope.upload = function () {
        // 异步上传文件
        baseService.uploadFile().then(function (response) {
            // 获取响应数据 {url : '', status : 200|500}
            if (response.data.status == 200){ // 上传成功
                // picEntity : {"color":"香槟色","url":"http://image.pinyougou.com/jd/wKgMg1qucV6AMJimAAHHMQZbqCM343.jpg"}
                $scope.picEntity.url = response.data.url;
            }else{ // 上传失败
                alert("文件上传失败！");
            }
        });
    };

    // 保存图片到图片数组
    $scope.addPic = function () {
        $scope.goods.goodsDesc.itemImages.push($scope.picEntity);
    };
    // 从图片数组中删除图片
    $scope.removePic = function (idx) {
        $scope.goods.goodsDesc.itemImages.splice(idx, 1);
    };

    // 根据父级id查询商品分类
    $scope.findItemCatByParentId = function (parentId, name) {
        // 发送异步请求
        baseService.sendGet("/itemCat/findItemCatByParentId?parentId="
            + parentId).then(function(response){
                // 获取响应数据 [{},{}]
                $scope[name] = response.data;
        });
    };

    // $scope.$watch(): 用$watch()方法来监听$scope中的变量，如果变量值发生改变，就会调用回调函数
    // 监听商品一级分类id发生改变，查询商品二级分类
    $scope.$watch("goods.category1Id", function (newVal, oldVal) {
        // alert("新的值：" + newVal + "，旧的值：" + oldVal);
        // 判断新的值不是undefined,就发送异步请求查询二级分类
        if (newVal){ // 不是undefined
            $scope.findItemCatByParentId(newVal, "itemCat2");
        }else{ // 是undefined
            // 清空二级分类变量
            $scope.itemCat2 = [];
        }
    });

    // $scope.$watch(): 用$watch()方法来监听$scope中的变量，如果变量值发生改变，就会调用回调函数
    // 监听商品二级分类id发生改变，查询商品三级分类
    $scope.$watch("goods.category2Id", function (newVal, oldVal) {
        // alert("新的值：" + newVal + "，旧的值：" + oldVal);
        // 判断新的值不是undefined,就发送异步请求查询三级分类
        if (newVal){ // 不是undefined
            $scope.findItemCatByParentId(newVal, "itemCat3");
        }else{ // 是undefined
            // 清空三级分类变量
            $scope.itemCat3 = [];
        }
    });

    // $scope.$watch(): 用$watch()方法来监听$scope中的变量，如果变量值发生改变，就会调用回调函数
    // 监听商品三级分类id发生改变，查询模板id
    $scope.$watch("goods.category3Id", function (newVal, oldVal) {
        // 判断新的值不是undefined,从三级分类数组中查询用户选择的三级分类对象，获取模板id
        if (newVal){   // 不是undefined
            // $scope.itemCat3 [{],{}]
            // 迭代三级分类数组
            for (var i = 0; i < $scope.itemCat3.length; i++){
                // 获取一个数组元素 ItemCat {id : '' name : '' , parentId : '' typeId: {}}
                var itemCat = $scope.itemCat3[i];
                // 判断是否为用户选中的
                if (itemCat.id == newVal){
                    // 获取模板id
                    $scope.goods.typeTemplateId = itemCat.typeId;
                }
            }
        }else{ // 是undefined
            $scope.goods.typeTemplateId = null;
        }
    });


    // 监听类型模板id发生改变，查询类型模板对象TypeTemplate
    $scope.$watch("goods.typeTemplateId", function (newVal, oldVal) {
        // 判断新的值不是undefined
        if (newVal){   // 不是undefined

            // 1. 从类型模板表根据id查询类型模板对象(一行数据)
            baseService.sendGet("/typeTemplate/findOne?id=" + newVal).then(function(response){
                // 获取响应数据 TypeTemplate {}
                // 1.1 获取品牌数据 json数组的字符串 解析成 json数组
                $scope.brandIds = JSON.parse(response.data.brandIds);

                // 1.2 获取扩展属性
                $scope.goods.goodsDesc.customAttributeItems =
                    JSON.parse(response.data.customAttributeItems);
            });

            // 2. 根据模板id,查询规格与规格选项数据
            baseService.sendGet("/typeTemplate/findSpecByTemplateId?id="
                + newVal).then(function(response){
                // 获取响应数据
                // [{"id":27,"text":"网络", options : [{},{}]},
                // {"id":32,"text":"机身内存",  options : [{},{}]}]
                $scope.specList = response.data;
            });


            // 清空用户选中的规格选项数组
            $scope.goods.goodsDesc.specificationItems = [];

        }else{ // 是undefined

        }
    });

    /** 记录用户选中的规格选项 */
    $scope.updateSpecAttr = function ($event, specName, optionName) {
        /**
         * $scope.goods.goodsDesc.specificationItems:
         * [{"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"},
         * {"attributeValue":["64G","128G"],"attributeName":"机身内存"}]
         */
        // 从$scope.goods.goodsDesc.specificationItems数组中根据attributeName查询一个json对象
        var obj = $scope.searchJsonFromArray($scope.goods.goodsDesc.specificationItems,
            "attributeName", specName);
        if (obj){ // 不是null
            // obj: {"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"}
            // 判断checkbox是否选中
            if ($event.target.checked){  // 选中
                // 往数组中添加元素
                obj.attributeValue.push(optionName);
            }else{ // 没选中
                // 获取optionName 在attributeValue数组中的索引号
                var idx = obj.attributeValue.indexOf(optionName);
                // 根据索引号删除数组中的元素
                obj.attributeValue.splice(idx,1);

                // 判断数组的长度是否为零
                if (obj.attributeValue.length == 0){
                    // 获取{"attributeValue":[],"attributeName":"机身内存"}在数组中的索引号
                    var idx = $scope.goods.goodsDesc.specificationItems.indexOf(obj);
                    // 从$scope.goods.goodsDesc.specificationItems数组中删除元素
                    $scope.goods.goodsDesc.specificationItems.splice(idx,1);
                }
            }

        }else{ // 是null
            $scope.goods.goodsDesc.specificationItems
                .push({"attributeValue":[optionName],"attributeName":specName});
        }
    };

    /** 从数组中查询一个json对象 */
    $scope.searchJsonFromArray = function (jsonArr, key, value) {
        /**
         * jsonArr:
         * [{"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"},
         * {"attributeValue":["64G","128G"],"attributeName":"机身内存"}]
         */
        for (var i = 0; i < jsonArr.length; i++){
            // 得到一个数组元素
            // {"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"}
            var json = jsonArr[i];
            // json[key]:  json.attributeName == "网络"
            if (json[key] == value){
                return json;
            }
        }
        return null;
    };

    /** 根据用户选中的规格选项数组生成SKU数组 */
    $scope.createItems = function () {
        // 1. 先初始化SKU数组
        // spec: {"网络":"联通4G","机身内存":"64G"}
        // {spec : {}, price : 0, num : 0, status : '0', isDefault : '0'} : Item.java tb_item
        $scope.goods.items = [{spec : {}, price : 0, num : 9999, status : '0', isDefault : '0'}];

        // 2. 获取用户选中的选中的规格选项数组
        // $scope.goods.goodsDesc.specificationItems
        // [{"attributeValue":["移动4G","联通3G","联通4G"],"attributeName":"网络"}]
        var specItems = $scope.goods.goodsDesc.specificationItems;

        // 3. 循环选中的规格选项数组生成SKU数组中元素
        for (var i = 0; i < specItems.length; i++){
            // 获取数组中的一个元素
            // {"attributeValue":["移动4G","联通3G","联通4G"],"attributeName":"网络"}
            var json = specItems[i];
            // 生成SKU数组
            $scope.goods.items = $scope.swapItems($scope.goods.items,
                json.attributeValue, json.attributeName);
        }

    };

    /** 转化$scope.goods.items 数组中元素 根据 attributeValue数组中的元素生成 一个新的SKU数组 */
    $scope.swapItems = function (items, attributeValue, attributeName) {
        // items : [{spec : {}, price : 0, num : 0, status : '0', isDefault : '0'}]
        // attributeValue: ["移动4G","联通3G","联通4G"]
        // attributeName: 网络
        // 定义新的SKU数组
        var newItems = [];

        // 1. 先循环以前的SKU数组
        for (var i = 0; i < items.length; i++){// 3
            // 获取一个item对象
            // {spec : {}, price : 0, num : 0, status : '0', isDefault : '0'}
            var item = items[i];

            // 2. 循环一个规格选项名称的数组  ["移动4G","联通3G","联通4G"] ["16G"]
            for (var j = 0; j < attributeValue.length; j++){
                // 获取一个元素
                var optionName = attributeValue[j];

                // 把item转化成一个新的item
                var newItem = JSON.parse(JSON.stringify(item));
                // newItem.spec: {"网络":"联通4G"}
                newItem.spec[attributeName] = optionName;

                newItems.push(newItem);
            }
        }
        return newItems;
    };


    /** 查询条件对象 */
    $scope.searchEntity = {};
    /** 分页查询(查询条件) */
    $scope.search = function(page, rows){
        baseService.findByPage("/goods/findByPage", page,
			rows, $scope.searchEntity)
            .then(function(response){
                /** 获取分页查询结果 */
                $scope.dataList = response.data.rows;
                /** 更新分页总记录数 */
                $scope.paginationConf.totalItems = response.data.total;
            });
    };

    // 定义状态码提示的数组
    $scope.status = ['未审核','已审核','审核未通过','关闭']


    /** 商品上下架 */
    $scope.updateMarketable = function(status){
        if ($scope.ids.length > 0){
            baseService.sendGet("/goods/updateMarketable?ids="
                + $scope.ids + "&status=" + status)
                .then(function(response){
                    if (response.data){
                        /** 重新加载数据 */
                        $scope.reload();
                        // 清空ids数组
                        $scope.ids = [];
                    }else{
                        alert("商品上下架失败！");
                    }
                });
        }else{
            alert("请选择要上下架的商品！");
        }
    };
});