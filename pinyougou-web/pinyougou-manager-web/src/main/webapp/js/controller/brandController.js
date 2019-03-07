// 定义品牌控制器层
app.controller('brandController', function ($scope, $controller, baseService) {

    // 指定继承baseController
    $controller('baseController', {$scope : $scope});


    // 定义分页查询品牌
    $scope.search = function (page, rows) {
        // alert(JSON.stringify($scope.searchEntity));
        baseService.findByPage("/brand/findByPage", page, rows,
            $scope.searchEntity).then(function(response){
            // 获取响应数据
            // response.data: {total : 100, rows : [{},{}]}
            // {total : 100, rows : [{},{}]} --> Map<String, Object>
            // {} Map|对象
            // [] List
            $scope.dataList = response.data.rows;
            // 更新分页指令中的总记录数
            $scope.paginationConf.totalItems = response.data.total;
        });
    };

    // 添加或修改品牌
    $scope.saveOrUpdate = function () {
        //alert(JSON.stringify($scope.entity));
        // 判断品牌id
        var url = "save"; // 添加品牌
        if ($scope.entity.id){ // 修改品牌
            url = "update";
        }
        baseService.sendPost("/brand/" + url, $scope.entity)
            .then(function(response){
            // 获取响应数据 true|false
            if (response.data){ // true
                // 重新加载数据
                $scope.reload();
            }else{
                alert("添加失败！");
            }
        });
    };

    // 修改按钮点击事件
    $scope.show = function (entity) {
        // 把entity json对象转化成字符串
        var jsonStr = JSON.stringify(entity);
        // 把jsonStr json字符串转化成json对象
        $scope.entity = JSON.parse(jsonStr);
    };


    // 删除品牌
    $scope.delete = function () {
        // 判断用户是否选中了需要删除的品牌
        if ($scope.ids.length == 0){
            alert("请选择要删除的品牌！");
        }else{
            if (confirm("亲，您确定要删除吗？")){
                // 发送异步请求
                baseService.deleteById("/brand/delete", $scope.ids)
                    .then(function(response){
                    // 获取响应数据 true|false
                    if (response.data){
                        // 清空ids数组
                        $scope.ids = [];
                        // 重新加载数据
                        $scope.reload();
                    }else{
                        alert("删除出错了！");
                    }
                });
            }
        }
    };

});