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
    };
    /** 判断某个规格选项是否被选中 */
    $scope.isSelected = function(name, value){
        return $scope.specItems[name] == value;
    };

})