/** 定义搜索控制器 */
app.controller("searchController" ,function ($scope, $sce, baseService) {

   /** 定义搜索参数对象 */
   $scope.searchParam = {keywords : '',category : '',
       brand : '',price : '',spec : {}, page : 1, rows : 5};
   /** 定义搜索方法 */
   $scope.search = function () {
       baseService.sendPost("/Search",$scope.searchParam).then(function (response) {
           /** 获取搜索结果 */
           $scope.resultMap = response.data;

           // 初始化页码数组
           $scope.initPageNum();
       })
   }

    // 初始化页码数组
    $scope.initPageNum = function () {
        $scope.pageNums = [];

        // 开始页码
        var firstPage = 1;
        // 结束页码
        var lastPage = $scope.resultMap.totalPages;
        for (var i = firstPage; i <= lastPage ; i++){
            $scope.pageNums.push(i);
        }
    }

   $scope.trustHtml = function (html) {
       return $sce.trustAsHtml(html);
   }

   $scope.addSearchItem = function (key,value) {
        /** 判断是商品分类、品牌、价格 */
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchParam[key] = value;
        } else {
            /** 规格选项 */
            $scope.searchParam.spec[key] = value;
        }
        /** 执行搜索 */
        $scope.search();
   }

   /** 删除搜索选项的方法 */
   $scope.removeSearchItem = function (key) {
       /** 判断是商品分类、品牌、价格 */
       if (key == 'category' || key == 'brand' || key == 'price') {
           $scope.searchParam[key] = "";
       } else {
           delete $scope.searchParam.spec[key] ;
       }
       /** 执行搜索 */
       $scope.search();
   }
});
