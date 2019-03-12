// 定义商品详情页 控制层
app.controller('itemController',function ($scope,$controller) {
    $scope.addNum = function (x) {
        console.log("num :" + $scope.num);
        $scope.num += x;
        if($scope.num < 1) {
            $scope.num = 1;
        }
    }

    /** 记录用户选择的规格选项 */
    $scope.specItems = {};
    /** 定义用户选择规格选项的方法 */
    $scope.selectSpec = function(name, value){
        $scope.specItems[name] = value;
        /** 查找对应的SKU商品 */
        searchSku();
    };
    /** 判断某个规格选项是否被选中 */
    $scope.isSelected = function(name, value){
        return $scope.specItems[name] == value;
    };

    /** 加载默认的SKU */
    $scope.loadSku = function () {
        /** 取第一个SKU */
        $scope.sku = itemList[0];
        /** 获取*/
        $scope.specItems = JSON.parse($scope.sku.spec);
    }

    /** 根据用户选中的规格选项，查找对应的SKU商品 */
    var searchSku = function () {
        for (var i = 0; i < itemList.length; i++) {
            /** 判断规格选项是不是当前用户选中的 */
            if(itemList[i].spec == JSON.stringify($scope.specItems)) {
                $scope.sku = itemList[i];
                return;
            }
        }
    }

    /** 加入购物车事件绑定 */
    $scope.addToCart = function () {
        alert("skuid:" + $scope.sku.id);
    }
})