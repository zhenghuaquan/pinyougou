/** 基础控制器层 */
app.controller('baseController', function ($scope) {

    // 分页指令需要的配置信息json对象
    $scope.paginationConf = {
        currentPage : 1, // 当前页码
        totalItems : 0, // 总记录数
        itemsPerPage : 10, // 页大小
        perPageOptions : [10,15,20,25,30], // 页码下拉列表框
        onChange : function () { // 页码发生改变事件
            $scope.reload();
        }
    };

    // 定义重新加载数据的方法
    $scope.reload = function () {
        // 分页查询品牌
        $scope.search($scope.paginationConf.currentPage,
            $scope.paginationConf.itemsPerPage);
    };

    // 定义多个品牌id的数组
    $scope.ids = [];
    // 为checkbox绑定点击事件
    $scope.updateSelection = function ($event, id) {
        // $event.target : 获取checkbox对应的dom元素
        //alert($event.target.checked);
        // 判断checkbox是否选中
        if ($event.target.checked){ // true 选中
            // 往数组中添加元素
            $scope.ids.push(id);
        }else{ // false 没有选中
            // 从数组中删除元素
            // 第一个参数：元素在数组中的索引号
            var idx = $scope.ids.indexOf(id);
            // 第二个参数：删除的个数
            $scope.ids.splice(idx, 1);
        }
    };


    /** 提取数组中json某个属性，返回拼接的字符串(逗号分隔) */
    $scope.jsonArr2Str = function (jsonArrStr, key) {
        // 把jsonArrStr 字符串转化成 json数组
        // [{"id":1,"text":"联想"},{"id":3,"text":"三星"},{"id":2,"text":"华为"}]
        var jsonArr = JSON.parse(jsonArrStr);

        var resArr = [];

        for (var i = 0; i < jsonArr.length; i++){
            // 取数组元素 {"id":1,"text":"联想"}
            var json = jsonArr[i];
            resArr.push(json[key]);
        }
        // join: 将数组中的元素用什么分隔，返回一个字符串
        return resArr.join(",");
    };

});