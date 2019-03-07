/** 定义首页控制器层 */
app.controller("indexController", function($scope, baseService){

    /** 根据广告分类id查询广告内容 */
    $scope.findContentByCategoryId = function (index) {
        baseService.sendGet("/content/findContentByCategoryId?categoryId=" + index)
            .then(function (response) {
                $scope.contentList = response.data;
            });
    }
});