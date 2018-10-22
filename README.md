<!--deliverableStart-->
<template>
    <div class="deliverable">
        <div class ="top">
            
            <div class="row_right fr">
                <div class="row_status" v-if='false'>
                    <selected :list="typeL"
                              :value.sync="typeId" 
                              :name="'name'"
                              :sign="'id'" 
                              v-on:chose="choseStatus" 
                              placeholder="交付件类型"
                              ref="statusRef"></selected>
                </div>
                <div class="row_status">
                    <selected :list="statusL"
                              :value.sync="statusId" 
                              :name="'name'"
                              :sign="'id'" 
                              v-on:chose="choseStatus" 
                              placeholder="状态"
                              ref="statusRef"></selected>
                </div>
                <input class="search_input" v-model="searchKeyword" @keyup.13="searchBlur" placeholder="Type to Search" v-on:input="isreset(searchKeyword)">
                <img class="searchreset" src="../../assets/clear.png" @click = "searchreset" ref="resetimg" v-show="isediting"></img>
                <img class="search_btn" src="../../assets/filter_search.png" @click="searchBlur">

            </div>   
        </div>
        <div class="table">
            <el-table
                :data="deliverableList"
                tooltip-effect="dark"
                style="width: 100%;"
                max-height="800"
                empty-text=" "
                 @sort-change="sortOrder"
                border>
                <el-table-column
                  label="交付件名称" width="250" >
                  <template slot-scope="scope" >
                    <el-popover trigger="hover" placement="right-end">
                      <p>{{ scope.row.name }}</p>
                      <div slot="reference" class="name-wrapper">
                        <!--中转页面 -->
                        <span v-if="scope.row.type != '2'"><a :href="deliverableUrl + scope.row.id + '?status=' + scope.row.status" target="_blank">{{ scope.row.name }}</a></span>
                        <span v-else><a href="javascript:void(0);" @click="viewDeliverableDoc(scope.row)">{{ scope.row.name }}</a></span>
                      </div>
                    </el-popover>
                  </template>
                </el-table-column>
                <el-table-column
                    label="交付件类型">
                  <template slot-scope="scope">
                     <div slot="reference" class="name-wrapper">
                       <span>{{scope.row.type | deliverableType}}</span>
                     </div>
                  </template>
                </el-table-column>
                <el-table-column label="任务名称">
                    <template slot-scope="scope">
                        <el-popover trigger="hover" placement="right-end">
                            <p>{{ scope.row.taskName }}</p>
                            <div slot="reference" class="name-wrapper">
                                 <a  :href="'/project/detail/'+ scope.row.projectId + '?taskId=' + scope.row.taskId" target="_blank">{{ scope.row.taskName }}</a>
                            </div>
                        </el-popover>
                    </template>
                </el-table-column>
                <el-table-column
                    label="摘要"> 
                    <template slot-scope="scope">
                        <el-popover trigger="hover" placement="left">
                          <p> {{ scope.row.description }}</p>
                          <div slot="reference" class="name-wrapper">
                            <span >{{ scope.row.description }}</span>
                          </div>
                        </el-popover>
                    </template>
                </el-table-column>
                <el-table-column
                    label="状态">
                    <template slot-scope="scope">
                        <div class="status_td"><img :src="scope.row.statusUrl"><span>{{ scope.row.docStatus}}</span></div>
                    </template>
                </el-table-column>
                <el-table-column
                    label="作者"> 
                    <template slot-scope="scope">
                    	<el-popover trigger="hover" placement="left">
                          <p> {{ scope.row.author }}</p>
                          <div slot="reference" class="name-wrapper">
                            <span >{{ scope.row.author }}</span>
                          </div>
                        </el-popover>
                          <!-- espaceGroup :item="scope.row" :sysUserId="scope.row.author" :userName="scope.row.author" :title="''" :espaceMoreClass="'listUserNameCss'"></espaceGroup -->
                    </template>
                </el-table-column>
                <el-table-column 
                    label="完成时间"
                    sortable='custom' prop="finishTime">
                    <template slot-scope="scope" >
                        <span>{{ scope.row.finishTime | onlyDay }}</span>
                    </template>
                </el-table-column>
                <el-table-column 
                    label="操作"
                    >
                    <template slot-scope="scope" >
                        <div v-show="project.status=='0'">
                            <img src="../../assets/require/sharing.png" v-if="scope.row.isSharePower"  
                                style="margin-left :10px;cursor: pointer;" @click="showPublicBefore(scope.row)" title="分享">
                            <img v-if="scope.row.isRepublic" src="../../assets/require/re-enter-create.png"  style="margin-left :10px;cursor: pointer;"
                            	 @click="createIdpAgain(scope.row)" title="重新创建IDP文档">
                            <img src="../../assets/edit.png"  v-if="scope.row.isSharePower" 
                                style="margin-left :10px;cursor: pointer;" @click="goTaskTransit(scope.row)" title="编辑">
                        </div>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <pagination :total="total" :pageItem="pageItem"  @pagechange = "pagechange"  ref = "pag"></pagination>
        <page v-on:goPage='getResult' ref="page"></page>
        <msgModal ref='msg'></msgModal>
        <el-dialog v-dialogDrag title ="选择分享路径" size = "docSize" class = "docDialog" custom-class = "el-dialog--docSize"  :visible.sync = "docDialog" :close-on-click-modal = "false">
            <div class = "content_box">
                <div class="publish">
                    <span>分享路径:</span>
                    <el-select v-model="statusSelected" class="status" filterable placeholder="请选择" @change="searchMode">
						<el-option
						      v-for="item in Librarys"
						      :key="item.id"
						      :label="item.name"
						      :title="item.title"
						      :value="item.id"
						      :disabled="item.disabled">
						</el-option>
                    </el-select>
                </div>    
                <div class="library">
                    <div class="tree" v-if="docDialog">
                        <knowledge  ref = "require"  :arr = 'arr' :module = "module" :dirStatusMap="dirStatusMap" v-on:chosenRequire="chosenRequire" v-if="statusSelected == 'libbase'"></knowledge>
                        <prodocNav ref = "prodocNav" :arr = 'arr' v-if="statusSelected != 'libbase'" :dirStatusMap="dirStatusMap"  v-on:chosenProdocNav="chosenProdocNav"  :pId="pId"></prodocNav>
                    </div>
                    <div class="path">
                        <div class="prompt_1">
                            <span>已选择路径：</span>
                        </div>
                       <div class="prompt_2">
                            <div class="prompt_3"  v-for="(item,index)  in idList">
                                <span :title="item.title.length>45?item.title:''">{{item.path}}</span><img  src="../../assets/require/red_delect.png" @click="del(item,index)">
                             </div>   
                        </div> 
                    </div>
                </div>
                <span class="shareNotice">注：已分享的路径，如果重复分享，会覆盖分享路径下的该文档</span>
            </div>
            <div slot = "footer" class = "close">
                <button  @click="shareDoc" class = "btn btn-light">确定</button>
                <button @click="docDialog=false;" class = "btn btn-normal">取消</button>
            </div>
        </el-dialog>  

     <el-dialog v-dialogDrag title ="重新创建IDP文档" size = "idpSize" class = "docDialog" custom-class = "el-dialog--idpSize"  :visible.sync = "idpDialog" :close-on-click-modal = "false">   
        <div  class="clearBox" style="height: 150px;margin: 20px;">
            <div class="row" >
                <label class="left labelColor fl" >模板文档:</label>
                <div class="doc_box" :class="{'error_border':isTempdoc}">
                    <a href="javaScript:void(0);" @click="viewDoc(tempdocId)" :title="tempdocName">{{tempdocName}}</a>
                    <span class="clear_btn" @click="cleartempdoc" v-if="tempdocName">X</span>
                </div>
            </div>
            <div class="row">
                <div class="uploadOption">
                    <a href="javaScript:void(0);" @click="showDocDialog('0')">
                        <span class="upload"></span>
                        <span class="title">选择知识库文档</span>
                    </a>
                    <a href="javaScript:void(0);">
                        <span class="upload"></span>
                        <span class="title">选择本地文档
							<input type = "file" id = "fileChange" @change = "fileChange($event)" 
								ref = "inputer" accept='application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document'>
						</span>
                        
                    </a>
                </div>
            </div>
        </div>
        <div slot = "footer" class = "idpClose">
            <button @click="cancelReUploadIdp" class = "btn btn-normal">取消</button>
            <button  @click="submitReUploadIdp" class = "btn btn-light">确定</button>
        </div>
    </el-dialog>  

        <!--  文档助手/模板文档弹窗 -->
        <docDialog v-on:getdoc = "getdoc" ref="doc"></docDialog>


        <deliverableDialog  ref = "deliverDetail" :showDeliverableDialog = "showDeliverableDialog" :deliverableDetail = "deliverableDetail"
             @cancelShowDeliverableDetail="cancelShowDeliverableDetail"></deliverableDialog>
    </div>
</template>
<script>
    import knowledge  from '../assemblies/requireSelect.vue' 
    import deliverableDialog from '../assemblies/deliverableDetail.vue'
    import prodocNav from '../assemblies/prodocNav.vue'
    import docDialog from '../tasktemplate/docDialog.vue'
    export default{
		components:{deliverableDialog,knowledge,prodocNav,docDialog},   
        props:{
            project:{
                type:Object,
                default:{}
            }
        }, 
		data(){
            return{
                project:{},
                showDeliverableDialog:false,
                deliverableDetail : {},
                deliverableList:[],
                // contentInfoList:[],
                total:0,
                pageItem:[{text:20} , {text:50}, {text:100}],
                //交付件状态
                statusL:[
                {
                    name:'全部状态',
                    id:''
                },
                {
                    name:'IDP文档创建中',
                    id:'-1'
                },  
                {
                    name:'待分配',
                    id:'0'
                }, 
                {
                    name:'进行中',
                    id:'1'
                },     
                {
                    name:'已完成',
                    id:'2'
                },
                {
                    name:'IDP文档创建失败',
                    id:'4'
                }],
                statusId:'',//默认查询全部状态
                //交付件类型
                typeL:[
                {
                    name:'全部类型',
                    id:''
                },
                {
                    name:'文档',
                    id:'1'
                },
                {
                    name:'附件',
                    id:'2'
                }],
                typeId:'',//默认查询全部类型
                //搜索匹配值
                searchKeyword:"",
                oldkeywords:"",
                isediting:false,
                order:"desc",
                orderBy:'',
                obj:{
                    offset : 0,
                    limit : 20
                },
                projectId:'',
                userId:D.sysUid,
                //
                docDialog:false,
                idpDialog:false,
                publishDeliveId:'',
                statusSelected:"libbase",
                Librarys:[],
                initLib:[
                    {
                        "name":"知识库",
                        "id":"libbase",
                        "title":"知识库",
                        "projectId":""
                    }
                ],
                module:"Domain",
                idList:[],
                arr:[],
                tempdocId:"",
                tempdocName:"",
                isTempdoc:false,
                fileItems:[],
                deliverableId:"",
                //交付件类型
                deliverableType:"",
                isAdminOrOwner:false,
                taskUrl:"/project/task/transit/task/",
                deliverableUrl:"/project/task/transit/deliverable/",
                pId:"",
                idLibrarysMap:[],
                dirStatusMap:{},
                deliverableForReUploadIdp:{}
            }
        },
        created(){
            this.projectId = this.$route.query.projectId;
            this.increaseCount(this.projectId);
            this.isAdmin();
            this.$emit('initData');
        }, 
        
        filters:{
            deliverableType: function(value) {
                if(!value){
                    return;
                }
                switch (value){
                    case "1" : return "文档";
                    case "2" : return "附件";
                    default: return value;
                }
            },
        },
        watch:{
            'dltProModel'(newVal,oldVal){
                if(!newVal){
                    this.$store.state.require.methods ="";
                }
            }, 
            //选择模块 初始化
            'module'(newVal,oldVal){
                if(newVal){
                    this.$store.commit('initState');
                    this.$store.state.require.module = newVal;
                }
            }, 
            //节点
            'module'(newVal,oldVal){
                if(newVal){
                    this.$store.commit('initState');
                    this.$store.state.require.module = newVal;
                }
            },
        },
        methods:{
            //删除已勾选路径
            del(item,index){
                this.$store.state.require;
                if(this.idList.length>0)
                {
                    this.idList.splice(index,1);
                    document.getElementById(item.id).checked=false;
                }
                
            },
            //选择知识库
            chosenRequire(){
                if (this.$store.state.require && this.$store.state.require.strname && this.$store.state.require.strname.list){
                    this.idList  = this.$store.state.require.strname.list;
                }

            },
            //选择我的项目树结构
            chosenProdocNav(){
                if (this.$store.state.docNav && this.$store.state.docNav.diyid && this.$store.state.docNav.diyid.list){
                    this.idList  = this.$store.state.docNav.diyid.list;
                }
            },
            //切换知识库以及项目库
            searchMode(){
                this.arr=[];
                this.idList=[];
                this.$store.state.require.methods='';
                this.$store.state.docNav.methods ='';
                if(this.statusSelected != 'libbase'){
                    this.getSharedDirs(this.statusSelected);
                    //this.getProDocNav(this.statusSelected);
                }else{
                    this.getSharedDirs();
                }

            },
            initPublishStatus(dirlist,item,type,title){
                if(dirlist.includes(item.id)){
                    this.$set(item,type,true);
                    this.$set(item,'showTitle',title);
                }else{
                    this.$set(item,type,false);
                }
            },
            //获取根目录
            getProDocNav(directoryId){
                
                var self=this;
                self.$http.post("/project/directory/list/page.json",{
                    "supId":directoryId,
                }).then(res=>{
                    if(res && res.data && res.data.body && res.data.head && res.data.head.flag){
                        var directoryList=res.data.body.directoryList;
                        directoryList.forEach(function(item){
                            for(var key in self.dirStatusMap){
                                var dirlist=self.dirStatusMap[key];
                                if(key=='3'){
                                    self.initPublishStatus(dirlist,item,'isPublishing',"分享中");
                                }else if(key=='1'){
                                    self.initPublishStatus(dirlist,item,'isPublished',"分享成功");
                                }else if(key=='4'){
                                    self.initPublishStatus(dirlist,item,'isPublishFail',"分享失败");
                                }else{
                                    self.initPublishStatus(dirlist,item,'isRePublishFail',"重新分享失败");
                                }
                            }
                        });
                        self.$refs.prodocNav.itemData = directoryList;
                        var libraryObj = this.idLibrarysMap[directoryId];
                        if(libraryObj){
                            self.$refs.prodocNav.pid = libraryObj.projectId;
                        }
                    }else{
                        D.showMsg();
                    }    
                }).catch(function(){

                });     
            },
            viewDeliverableDoc(deliverable){
                if (deliverable.type == '2'){
                    console.log('打开附件交付件详情', deliverable);
                    // this.getContentInfo(deliverable.id);
                    this.deliverableDetail = deliverable;
                    this.showDeliverableDialog = true;
                    return;
                }
                if(deliverable.status == "-1"){
                    D.showMsg("IDP文档创建中，请稍后再试！");
                    return;
                }
                if(deliverable.status == "4"){
                    D.showMsg("IDP文档创建失败，请联系管理员！");
                    return;
                }
                let self = this;
                D.block();
                self.$http.get('/project/taskextension/getDeliverableUrl.json'+new Date().getTime()+'?deliverableId=' + deliverable.id).then(function(res){
                    D.unblock();
                    if(res && res.data && res.data.flag){
                        window.open(res.data.tips);
                    } else {
                        var errorcode = res.data.errorcode;
                        if (errorcode == "dfx.project.task.create.idpdoc.progress" ) {
                            D.showMsg("IDP文档创建中，请稍后再试！");
                        } else  if (errorcode == "dfx.project.task.finddoc.failed" ) {
                            D.showMsg("交付件文档信息查询失败，请联系管理员。");
                        } else if(errorcode == "dfx.project.task.create.idpdoc.failed") {
                            D.showMsg("idp文档创建失败，请联系管理员。");
                        } else if(errorcode == "dfx.project.task.deliverableType.url.failed") {
                            D.showMsg('链接未配置，请联系管理员。');
                        } else {
                            //D.showMsg();
                        }
                    }
                }).catch(function(){
                    D.unblock();
                    D.showMsg();
                })
            },
            // getContentInfo(deliverableId){
            //     let self = this;
            //     self.$http.get('/project/deliverable/getContentInfo.json?deliverableId=' + deliverableId).then(function(res){
            //         if(res && res.data && res.data.head && res.data.head.flag)
            //         {
            //             self.contentInfoList = res.data.body.contentInfoList;
            //         }
            //         else
            //         {
            //             D.showMsg("获取交付件信息失败。");
            //         }
            //     }).catch(function() {
            //         D.showMsg("获取附件信息失败。");
            //     })
            // },
            cancelShowDeliverableDetail(){
                this.showDeliverableDialog = false;
            },
            //排序
            sortOrder(){
                if(this.order=="desc"){
                    this.order="asc";
                }else{
                    this.order="desc";
                }
                this.$refs.page.goPage();
            },

            //列表数据获取请求
            getResult(obj){
                D.block();
                let self = this;
                if (obj) {
                    self.obj = obj;
                }
                if(this.oldkeywords != self.searchKeyword){
                    self.searchKeyword = this.oldkeywords;
                }
                var offset = self.obj.offset || 0;
                var limit = self.obj.limit || 20;
                this.$http.post('/project/deliverable/query/page.json'+new Date().getTime()+'?offset='+offset+"&limit="+limit,{
                    type:self.typeId,
                    status:self.statusId,
                    order:this.order,
                    orderBy:'finishTime',
                    description:self.oldkeywords,
                    projectId:self.projectId,
                    authorizedSysUid:self.userId
                }).then(function(res){

                    D.unblock();
                    res = res.data;
                    if(res && res.head && res.head.flag){
                        self.total = res.body.deliverableCount;
                        var userMap=res.body.userMap;
                        var deliverables = res.body.deliverableList;
                        if(self.total>0){
                            deliverables.forEach(function(item){
                                var userName=userMap[item.id];
                                if(self.isAdminOrOwner||userName.indexOf(D.userName)>=0){
                                	if(item.status == '4'){
	                                	self.$set(item,'isRepublic',true);
                                	}
                                	//不是附件类型的
                                    if((item.status == '1'||item.status == '2')&&item.type!='2'){
                                        self.$set(item,'isSharePower',true);
                                    }
                                }
                                if(item.status == "0"){
                                    self.$set(item,'docStatus','待分配');
                                    self.$set(item,'statusUrl',require('../../assets/require/status_btn_waitting.png'));
                                }
                                if(item.status == "2"){
                                    //item.status = "已完成";
                                    self.$set(item,'docStatus','已完成');
                                    self.$set(item,'statusUrl',require('../../assets/require/status_btn_finish.png'));
                                }
                                if(item.status == "1"){
                                    //item.status = "进行中";
                                    self.$set(item,'docStatus','进行中');
                                    self.$set(item,'statusUrl',require('../../assets/require/status_btn_processing.png'));
                                }
                                if(item.status == "-1"){
                                    //item.status = "创建中";
                                    self.$set(item,'docStatus','IDP文档创建中');
                                    self.$set(item,'statusUrl',require('../../assets/require/status_btn_creating.png'));
                                }
                                if(item.status == "4"){
                                    //item.status = "创建失败";
                                    self.$set(item,'docStatus','IDP文档创建失败');
                                    self.$set(item,'statusUrl',require('../../assets/require/status_btn_failed.png'));
                                }
                            });     
                        }
                        //self.setOptionPower(deliverables);
                        self.deliverableList = deliverables;       
                        
                    }else{
                        D.showMsg();  
                    }   
                }).catch(function(e){
                   // D.showMsg(e);
                });
            },
            //根据类型或者状态过滤
            choseStatus(){
                this.$refs.pag.setCurrent(1);
                this.getResult(this.obj);
            },
            setOptionPower(deliverables){
                deliverables.forEach(function(item){
                    //if(item.)
                });
            },
            isAdmin(){
                let self=this;
                this.$http.get('/project/member/isAdminOrOwner.json'+new Date().getTime()+'?projectId='+this.projectId)
                .then(function(res){
                    self.isAdminOrOwner=res.data;
                }).catch(function(e){
                    D.showMsg();
                });
            },
            //翻页
            pagechange(obj){
                //this.obj.offset = this.offset + 20;
                this.obj = obj;
                this.getResult(this.obj); 
            },

            //模糊搜索查询
            searchBlur(){
                this.offset = 0;
                this.oldkeywords = this.searchKeyword;
                this.$refs.pag.setCurrent(1);
                this.getResult(this.obj);
            },

            //重置
            isreset(searchKeyword){
                if(searchKeyword.length > 0){
                    this.isediting = true;
                }else{
                    this.isediting = false;
                }  
            },

            //查询按钮
            searchreset(){
                this.searchKeyword = "";
                this.oldkeywords = "";
                this.isediting = false;
                this.$refs.page.goPage();
            },
           increaseCount(projectId){
                this.$http.get('/project/project/increaseCount.json'+new Date().getTime()+'?projectId='+projectId)
            },
            showPublicBefore(item){
                let self = this;
                self.$http.get('/project/taskextension/getDeliverableUrl.json'+new Date().getTime()+'?deliverableId=' + item.id)
                .then(function(res){
                    if(res && res.data && res.data.flag){
                        self.showPublic(item);
                    } else {
                        var errorcode = res.data.errorcode;
                        if (errorcode == "dfx.project.task.create.idpdoc.progress" ) {
                            D.showMsg("idp文档创建失败，请联系管理员。");
                        } else  if (errorcode == "dfx.project.task.finddoc.failed" ) {
                            D.showMsg("idp文档创建失败，请联系管理员。");
                        } else if(errorcode == "dfx.project.task.create.idpdoc.failed") {
                            D.showMsg("idp文档创建失败，请联系管理员。");
                        } else if(errorcode == "dfx.project.task.deliverableType.url.failed") {
                            self.showPublic(item);
                        } else {
                            D.showMsg("idp文档创建失败，请联系管理员。");
                        }
                    }
                }).catch(function(){
                    D.showMsg();
                })
            },
            showPublic(item){
                this.statusSelected='libbase';
                this.$store.state.require.methods='';
                this.arr=[];
                this.idList=[];
                this.docDialog=true;
                this.publishDeliveId=item.id
                this.Librarys=[];
                this.Librarys.push(this.initLib[0]);
                this.deliverableType=item.deliverableType;
                let self=this;
                var project = {
                		'name': ''
                }
                this.$http.post('/project/project/getMyprojectExIndividual.json', project).then(function(res){
                    //self.Librarys
                    res=res.data;
                    if(res.head && res.head.flag){
                        var projList=res.body.message;
                        projList.forEach(function(item){
                            var title = item.name;
                            if(title.length>60)
                            {
                            	item.name = item.name.substring(0,50)+"...";
                            }

                            var libraryObj = {
                                "title":title,
                                "name":item.name,
                                "id":item.directoryId,
                                "projectId":item.pid
                            }
                            self.Librarys.push(libraryObj);
                            self.idLibrarysMap[item.directoryId] = libraryObj;
                        });
                        self.getSharedDirs();
                    }
                }).catch(function(){

                });
            },
            getSharedDirs(status){
                let self=this;
                var projectId=self.getSelectProjectId();
                this.$http.get('/project/deliverable/getSharedDirs.json'+new Date().getTime()+"?deliveId="+self.publishDeliveId+"&projectId="+projectId)
                    .then(function(res){
                        res=res.data;
                        if(res && res.head && res.head.flag){
                            self.dirStatusMap=res.body.dirStatusMap;
                            if(status){
                                self.getProDocNav(status);
                            }
                        }else{
                            D.showMsg();
                        }
                    })
                    .catch(function(){
                        D.showMsg();
                    });
            },
            getSelectProjectId(){
                var selectProjId=this.statusSelected;                
                if(this.statusSelected!='libbase'){                  
                    for (var i = 0; i < this.Librarys.length; i++) { 
                        if(this.Librarys[i].id==this.statusSelected){
                            selectProjId=this.Librarys[i].projectId; 
                            break;                                   
                        }                                            
                    };                                               
                }
                return  selectProjId;
            },
            //发布、分享
            shareDoc(){
                var dirId=[];
                if(this.idList.length==0){
                    D.showMsg("请选择分享目录");
                    return;
                }
                this.idList.forEach(function(item){
                    dirId.push(item.id);
                });

                var selectProjId =this.getSelectProjectId();                                                  
                var map={};
                map[selectProjId]=dirId;
                var data={
                    "deliveId":[this.publishDeliveId],
                    "map":map,
                    "deliverableType":this.deliverableType
                }
                D.block();
                let self=this;
                this.$http.post('/project/deliverable/shareDeliverable.json'+new Date().getTime(),data)
                .then(function(res){
                	D.unblock();
                    res=res.data;
                    console.log(res);
                    if(res.head && res.head.flag){
                        //D.showMsg("交付件分享成功");
                        self.docDialog=false;
                        return;
                    }else{
                        if(res.head.msg=='dfx.project.deliverable.node.creating'){
                            D.showMsg("交付件已经分享到该路径下，文档正在创建中。。。");
                        }else if(res.head.msg=="dfx.projectwebsite.deliverable.nodeList.empty"){
                            D.showMsg("交付件文档不存在");
                        }else{
                            D.showMsg("发布失败，请联系管理员");
                        }
                    }

                }).catch(function(){
                	 D.unblock();
                	 D.showMsg();
                });
            },
            //选择模板文档、文档助手
            showDocDialog(type){
                let self = this;
                this.$refs.doc.docDialog = true;
                this.$refs.doc.id = this.tempdocId;
                this.$refs.doc.dialogName = '模板文档';  
                this.$refs.doc.dialogType = type;
                this.$refs.doc.searchreset();
            },
            //回显模板文档
            getdoc(data){
                this.tempdocName = data.name;
                this.tempdocId = data.id;
                this.fileItems = [];
                if(this.tempdocId){
                  this.isTempdoc = false;      
                }  

            },
            //清除模板文档
            cleartempdoc(){
                this.tempdocName = "";
                this.tempdocId = "";  
                this.fileItems = [];
            },
            fileChange(inputEle){

                let self = this;

                let file = this.$refs.inputer.files;
                //解决IE11上传附件触发filechange事件两次的问题
                // if(file.length == 0 && self.contentInfoList.length == self.toDeletePartNoList.length){
                if(file.length == 0){
                    return;
                }
                //判断文件个数
            
                let formdata = new FormData();
                //文件插入到formData

                
              
                for(var i = 0;i < file.length;i++){
                    // 附件名称长度判断，不能超过80个字符
                    var name = file[i].name;
                    file[i].firstName = file[i].name.split('.')[0];
                    if(file[i].size < 1){
                        D.showMsg("选择的文件内容不能为空，请重新选择");
                        return this.$refs.inputer.value='';
                    }
                    if(file[i].size/1024/1024 > 500){
                        D.showMsg("选择的文件不能大于500M，请重新选择");
                        return this.$refs.inputer.value='';
                    }
                    if(!name.endsWith(".doc") && !name.endsWith(".docx")){
                        D.showMsg("选择的文件只能选择Word文档，请重新选择");
                        return this.$refs.inputer.value='';
                    }
     
                    formdata.append('file',file[i]);
                }; 
                this.$refs.inputer.value = '';
                //发送formdata到临时目录，返回一个数组
                D.block();
                this.$http.post('/project/upload/tempAttachments.json',formdata,{
                    headers:{
                        "Content-Type": "multipart/form-data"
                    }
                }).then(res => {
                    D.unblock();
                    res = res.data;
                    if(res.head && res.head.flag){
                        self.fileItems = res.body.message;
                        if(self.fileItems){
                            var fileName = self.fileItems[0].FILE_NAME;
                            //模版文档
                            self.tempdocName = self.fileItems[0].FILE_NAME;
                            self.tempdocId = "";
                        }
                    } else {
                         D.showMsg("添加文件失败");
                    }
                }).catch(res => {
                  D.showMsg("服务器错误，请重新上传.")})
            },
            submitReUploadIdp(){
                var self = this;
                var taskInfo = {
                    templateDoc:this.tempdocId,     //模板文档NodeId
                    //交付件属性
                    deliverable:{
                        // id:this.deliverableId
                        id:this.deliverableForReUploadIdp.id
                    },
                    //本地文件
                    fileItems:this.fileItems,
                    taskId:this.deliverableForReUploadIdp.taskId
                }

                if((!this.fileItems || this.fileItems.length == 0) && !this.tempdocId){
                    D.showMsg('是否创建空idp模版文档？',function(){
                        self.doReUploadIdp(taskInfo);
                    },true,false);
                    return ;
                }

                this.doReUploadIdp(taskInfo);


                
            },
            doReUploadIdp(taskInfo){
                D.block();
                this.$http.post("/project/taskextension/reUploadIdp.json", taskInfo, {
                    contentType:'application/json',
                }).then(res=>{
                    D.unblock();
                    res=res.data;
                    if(res.head.flag){
                        //重置
                        this.cancelReUploadIdp();
                        this.getResult(this.obj);
                    } else {
                        D.showMsg(res.head.msg);
                    }
                }).catch(function(){
                    D.unblock();
                    D.showMsg('重新创建IDP文档失败,网络服务异常,请联系管理员!');
                });
            },
            cancelReUploadIdp(){
                this.idpDialog = false;
                this.fileItems = [];
                this.tempdocId = "";
                this.tempdocName = "";
            },
            createIdpAgain(deliverable){
                this.deliverableId = deliverable.id;
                this.deliverableForReUploadIdp = deliverable;
                this.idpDialog = true;
            },
            goTaskTransit(deliverable){
                window.open(this.taskUrl + deliverable.taskId);
            }

    }
}

</script>
<!--deliverableEnd-->
<!--batchImportStart-->
    <div class = "batchImport">
        <!-- 文档助手弹窗 -->
        <el-dialog v-dialogDrag title = "批量导入" size = "docSize" class = "docDialog" custom-class = "el-dialog--docSize" :visible.sync = "relatedDocDialog" :close-on-click-modal = "false">
            <!-- <div class="upload">
                <img src="../../assets/upload.png" class="uploadimg"><span class="uploadtext">上传文件</span></img>
                <span class="text">说明:单个附件上传大小不超过500M,一次提交文件个数不超过20个</span>
            </div> -->
            <div class = "content_box">
                <div class="row_complete">
                    <div class="row_fr">
                        <button type="button" name="file" class="file_button"  @mouseover = "uploadMuen = true;">
                            <i class="icon-uploadfile"></i> 
                                <span id="upload_ie789_64_text_option"> 上 &nbsp;&nbsp;传 &nbsp;</span>
                                <span class="folder_caret"></span>
                        </button>
                        <ul class="uploadType" v-show="uploadMuen"  @mouseover.stop = "uploadMuen = true"  @mouseout.stop = "uploadMuen = false">
                            <li>
                                <label for = "file">文&nbsp;&nbsp;&nbsp;&nbsp;件</label>
                                <input class="file_input" type = "file" id = "file" multiple="multiple" @change = "fileChangeValidate($event, 'file')" ref = "file" >
                            </li>
                            <li v-if="isChrome">
                                <label for = "fileFolder">文件夹</label>
                                <input class="file_input" type="file" id="fileFolder" accept="" @change="fileChangeValidate($event, 'folder')" ref="folder" webkitdirectory directory mozdirectory>
                            </li>
                        </ul>
                        <div class="deleteBtn cursor" @click="deleteDoc(null,true)">
                            <img  class="left" src="../../assets/project/delete_iocn_white.png"></img>
                            <span style="margin-right:2px;">删除</span>
                        </div>
                    </div>
                </div>
                <div class="row_complete">
                    <span class="remark">说明：单个附件上传大小不可超过500M，一次提交文件个数不超过1000个</span>
                </div>
                <div class="row_complete">
                    <div class="prompt_div">
                        <span class="prompt">信息安全提示：</span>
                        <span class="prompt_text">禁止上传关键信息资产，如有敏感内容应隐去相关内容或以* 替代。</span>
                    </div>    
                </div>
                <div class="row_complete status_div">
                    <span class="uploadfile">上传文件<span class="uploadtext1">{{uploadFilesCount}}</span>个，</span>
                    <span>上传成功<span class="uploadtext1">{{successCount}}</span>个，</span>
                    <span>上传失败<span class="uploadtext1">{{failureCount}}</span>个，</span>
                    <span>正在上传中<span class="uploadtext1">{{uploadingCount}}</span>个。</span>
                    <div class="select_div">
                       <selected :list="statusList"
                              :value.sync="status" 
                              :name="'name'"
                              :sign="'id'" 
                              v-on:chose="statusFilter()" 
                              placeholder="全部状态"
                              ref="statusRef">
                        </selected>
                    </div>
                </div>
            <!--div class = "content_box"-->
                <div class="search_right fr">
                </div>
                <div class="table doctable">
                    <el-table
                        :data="fileItems" 
                        height = "450"
                        border
                        @selection-change="handleSelectionChange"
                        style="width: 100%">
                        <el-table-column
                          type="selection"
                          width="55">
                        </el-table-column>
                        <el-table-column
                          label="文档标题" width="200">
                          <template slot-scope="scope">
                            <el-popover trigger="hover" placement="right-end">
                              <p>{{ scope.row.fileName }}</p>
                              <div slot="reference" class="name-wrapper">
                                <span>{{ scope.row.fileName }}</span>
                              </div>
                            </el-popover>
                          </template>
                        </el-table-column>
                        <el-table-column label="状态" width="100">
                            <template slot-scope="scope">
                                 <img class="uploadStatus" src='../../assets/success.png' v-if="scope.row.status == 1" />
                                 <img class="uploadStatus" src='../../assets/1.png' v-if="scope.row.status == 0" />
                                 <img class="uploadStatus" src='../../assets/fail.png' v-if="scope.row.status == 2" />{{scope.row.status | turnUploadStatus}}
                            </template>
                        </el-table-column>
                        <el-table-column label="类型" width="100">
                            <template slot-scope="scope">
                                 <span>{{ scope.row.fileTypeName }}</span>
                            </template>
                        </el-table-column>
                        <!-- el-table-column label="作者" width="150">
                             <template slot-scope="scope">
                                 <div class="right fl11111" style="width:100%;" >
                                     <jobNumber  :class="{'slt_error':isAuthor}" :type="'input'" :required="true" ref="author"  :sign.sync="scope.row.author" v-on:blurEvent="authorBlur" ></jobNumber>
                                 </div>
                            </template>
                        </el-table-column -->
                        <el-table-column label="标签" width="522">
                            <template slot-scope="scope">
                               <div class="right fl11111" style="width:100%;" @click.stop = "changeTolabel(scope.row)">
                            <el-autocomplete
                                ref="autocomplete"
                                class="labelInput"
                                 v-model.trim="scope.row.keyword"
                                :fetch-suggestions="querySearch"
                                 placeholder="字符数少于20个,且不能包含以下字符 *  : ; ? , &quot; &lt; &gt; | \ /"
                                :trigger-on-focus="false"
                                @keydown.native.13 = "keydownEvent($event,scope.row)"
                                @select="handleSelect($event,scope.row)"
                                @blur="Blur($event,scope.row)"
                                ></el-autocomplete>
                                <!-- @keydown.native="keydownEvent($event,scope.row)" -->
                            <div  class="label"  >
                                <div class = "labelbox" 
                                    v-for="(item,index) in scope.row.labels" 
                                    :key="index"   
                                   
                                    
                                    >
                                    <a @click.stop = "handleSelect($event,scope.row)" ><span class="textlabelsapn" :title="item">{{item}}</span> <img src="../../assets/tab_close.png" @click.stop = "dellable(item,index,scope.$index,scope.row.labels)"></a>
                                </div>
                            </div>
                        </div> 
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="60">
                            <template slot-scope="scope">
                                <span ><img @click="deleteDoc(scope.row,false)" src='../../assets/delete-btn.png'/></span>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </div>
            <div slot = "footer" class = "close">
                <button @click = "save" class = "btn btn-light">保存</button>
                <button @click = "relatedDocDialog = false" class = "btn btn-normal">取消</button>
            </div>
        </el-dialog>
        <el-dialog size = "tiny" :modal = "false" :visible.sync="showModal"  style = "top:20%;width:700px;left:34%;" 
            custom-class = "el-dialog--confirm" >
            <div style = "text-align: center">{{text}}</div>                    
            <span slot="footer" class="dialog-footer">
              <button class="btn btn-light" style = "margin-top:20px;" @click="showModal = false">确定</button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
    export default{
        // components:{labelNumber},
        data(){
            return {
                isChrome:D.getExploreName() == "Chrome",
                uploadMuen:false,
                projectId:"",
                //相关资料,当前页所有的doc
                // fileList:[],
                selectList:[],
                //文档助手ID
                relatedDocDialog:false,
                total:0,
                keywords:"",
                oldkeywords:"",
                isediting:false,
                //本页面选择的doc
                checkDocList:[],
                nids:[],
                showModal:false,
                // 批量上传文件总和，用于页面显示
                fileItems:[],
                // 批量上传文件总和，用于提交创建文档
                fileList:[],
                //fileItemsInit:[],
                // 文件列表
                fileInfoList:[],
                // 上传临时目录文件信息集合
                multiFileList:[],
                dirId:"",
                dirName:"",
                batchNo:"",
                taskId:"",
                successCount:0,
                failureCount:0,
                uploadingCount:0,
                uploadFilesCount:0,
                num:0,
                copyItems:[],
                items:[],
                uploading:false,
                uploadSuccess:false,
                uploadFailure:false,
                flag:false,
                labelArray:[],
                // 分页条
                pageItem:[{text:"10"},{text:"20"},{text:"30"}],
                //
                showlabel:false,
                keyword:'',
                //输入框
                inputVal:'',
                directoryItem:{},
                statusList:[
                    {
                        name:"全部状态",
                        id:""
                    },
                    {
                        name:"上传中",
                        id:0
                    },
                    {
                        name:"上传成功",
                        id:1
                    },
                    {
                        name:"上传失败",
                        id:2
                    }],
                nodes:[],//当前目录下的文档
                repeatFileName:[],
            }
        },
        created:function(){
            this.projectId = this.$route.query.projectId;
        }, 
        methods:{
            //选中值
            handleSelectionChange(val) {
                this.selectList = val;
            },
            changeTolabel(item){
                item.showlabel = false;
                //item.keyword='';
                // let lableValue = "";
                item.label = "";
                item.labels.forEach(function(unit){
                    item.label += unit + ';';
                });  
                // this.$set(item,'label',lableValue);
                // this.$refs.labels.autofocus = "autofocus";
            },
            // showLabels(labels){
            //     if(labels.length > 0){
            //         this.showlabel = true;
            //     };
            // },
            statusFilter()
            {
                var status = this.status;
                if (status || status === 0)
                {
                    var tempList = [];
                    for (var i in this.fileList)
                    {
                        var fileItem = this.fileList[i];
                        if (fileItem && fileItem.status == status)
                        {
                            tempList.push(fileItem);
                        }
                    }
                    this.fileItems = tempList;
                }
                else
                {
                     this.fileItems = this.fileList;
                }
            },
            //设置标签数组
         
            // 删除标签
            dellable(lable,num,number,labels){
                var items = this.fileItems[number].labels;
                if(items && items.length > 0){
                    items.forEach(function(item,index){
                        if(index == num){
                          items.splice(num,1);
                        }
                    })
                }
                this.$set(this.fileItems[number],'labels',items);
                console.log("items",items)
            //     this.fileItems[number].labels.forEach((item,index)=>{
            //         if(index == num){
            //         this.fileItems[number].labels.splice(num,1);
            //     }
            // })
            },
           
            // 删除附件
            deleteDoc(item, isBatch){
                var fileIds = new Array();
                if (isBatch)
                {
                    if (this.selectList && this.selectList.length == 0)
                    {
                        D.showMsg("请选择要删除的附件！");
                        return;
                    }
                    for (var j = this.selectList.length - 1; j >= 0; j--)
                    {
                        var id = this.selectList[j].id;
                        if (id)
                        {
                            for (var i = this.fileItems.length-1; i>=0; i--)
                            {
                                var fileId = this.fileItems[i].id;
                                if (fileId && fileId == id)
                                {
                                    this.setCount(i);
                                    this.fileItems.splice(i, 1);
                                    fileIds.push(fileId);
                                    break;
                                }
                            }
                        }
                        else
                        {
                            for (var i = this.fileItems.length-1; i>=0; i--)
                            {
                                var fileName = this.fileItems[i].fileName;
                                var name = this.selectList[j].fileName;
                                if (fileName && fileName == name)
                                {
                                    this.setCount(i);
                                    this.fileItems.splice(i, 1);
                                    break;
                                }
                            }
                        }
                    }
                }
                else
                {
                    var id = item.id;
                    if (id)
                    {
                        for (var i = this.fileItems.length-1; i>=0; i--)
                        {
                            var fileId = this.fileItems[i].id;
                            if (fileId && fileId == id)
                            {
                                this.setCount(i);
                                this.fileItems.splice(i, 1);
                                fileIds.push(fileId);
                                break;
                            }
                        }
                    }
                    else
                    {
                        for (var i = this.fileItems.length-1; i>=0; i--)
                        {
                            var fileName = this.fileItems[i].fileName;
                            var name = item.fileName;
                            if (fileName && fileName == name)
                            {
                                this.setCount(i);
                                this.fileItems.splice(i, 1);
                                break;
                            }
                        }
                    }
                }
                
                if(fileIds.length > 0)
                {
                    this.deleteDocPost(fileIds);
                }
                
                if (this.$refs.folder)
                {
                    this.$refs.folder.value = "";
                }
                
                if (this.$refs.file)
                {
                    this.$refs.file.value = "";
                }
            },
            // 设置计数器
            setCount(index){
                this.uploadFilesCount--;
                if (1 == this.fileItems[index].status)
                {
                    this.successCount--;
                }
                else if (2 == this.fileItems[index].status)
                {
                    this.failureCount--;
                }
                else
                {
                    this.uploadingCount--;
                }
            },
            // 删除操作请求
            deleteDocPost(fileIds){
                this.$http.post('/project/upload/deleteUploadFile.json?fileIds=' + fileIds).then(function(res){
                    if (res)
                    {
                        var res = res.data;
                        if (res)
                        {
                            var head = res.head;
                            if (head && head.flag)
                            {
                                //D.showMsg("删除成功。");
                            }
                            else
                            {
                                D.showMsg("删除失败。");
                            }
                        }
                        else
                        {
                            D.showMsg();
                        }
                    }
                    else
                    {
                        D.showMsg();
                    }
               })
            },
            //获取全部文档
            fileChangeValidate(inputEle, type){
                var self=this;
                var directoryIds=[];
                directoryIds.push(self.dirId);
                self.$http.post("/project/document/getDirectoryNode.json",directoryIds,{
                    cache:false,
                    async:false,
                    contentType:'application/json;charset=UTF-8'
                }).then(res=>{
                    if(res && res.data && res.data.head && res.data.head.flag){
                        self.nodes=res.data.body.nodes;
                        self.fileChange(inputEle, type)
                    }else{
                        D.showMsg();
                    }  
                }).catch(e=>{console.log(e);});
            },
            // 上传文件及文件夹（type为file时，是上传文件；type为folder时，是上传文件夹）
            
            fileChange(inputEle, type)
            {
                var self=this;
                let files = this.$refs.file.files;
                if (type && "folder" == type)
                {
                    files = this.$refs.folder.files;
                }
                
                if (!files)
                {
                    return;
                }
                
                var fileCnt = files.length;
                // 文件夹下面没有文件
                if (fileCnt == 0)
                {
                    return;
                }
               
                // 文件个数大于1000
                if (fileCnt > 1000)
                {
                    D.showMsg("最多允许上传1000个文件，当前文件个数：" + fileCnt);
                    return;
                }
                //重名文件置空
                self.repeatFileName=[];
                var filesListTemp = [];
                for (var i = 0; i < files.length; i++)
                {
                    var fileSize = parseInt(files[i].size);
                    if (fileSize < 1)
                    {
                        D.showMsg("文件大小为0KB，不允许上传空文件。" + files[i].name);
                        return;
                    }
                    else if (fileSize > 524288000)
                    {
                        D.showMsg("上传的单个文件大小不能超过500M。");
                        return;
                    }
                    else
                    {
                        // 获取上传文件的名字
                        var fileName = files[i].name;
                        var uploadedFlag = false;
                        for (var j in this.fileList)
                        {
                            var fileItem = this.fileList[j];
                            if (fileItem.fileName == fileName && fileItem.fileSize == fileSize)
                            {
                                 uploadedFlag = true;
                                 break;
                            }
                        }
                        for (var k in self.nodes){
                            if(fileName==self.nodes[k].name){
                                uploadedFlag = true;
                                self.repeatFileName.push(fileName);
                                break;
                            }
                        }
                        if (uploadedFlag)
                        {
                            continue;
                        }
                        // 封装文件对象，用于上传立即在页面上显示
                        var fileTemp = {"fileSize":fileSize,"fileName":fileName,"status":0,"showlabel":false,"keyword":''};
                        // 设置标签数组属性
                        this.$set(fileTemp,'labels',new Array());
                        this.$set(fileTemp,'idList',new Array());
                        filesListTemp.push(fileTemp);
                        
                    }
                }
                if(self.repeatFileName && self.repeatFileName.length>0){
                    //清空本次上传的文件
                    if (this.$refs.folder.value)
                    {
                        this.$refs.folder.value = "";
                    }
                    if (this.$refs.file.value)
                    {
                        this.$refs.file.value = "";
                    }
                    D.showMsg("上传文件失败，上传的文件中存在与该节点下已有的文件重名。重名文件有："+self.repeatFileName.join("，").toString());
                    return;
                }
                // 上传文件总数加+1
                this.uploadFilesCount=this.uploadFilesCount+filesListTemp.length;
                // 上传中文件总数+1
                this.uploadingCount=this.uploadingCount+filesListTemp.length;
                console.log("this.fileList",this.fileList);
                // 将本次上传的文件信息合入到
                this.fileList = this.fileList.concat(filesListTemp);
                this.fileItems = this.fileList;
                this.$refs.statusRef.reset();
                //批次号生成成功后，上传文件到临时目录并且创建定时器刷新上传的状态，直到上传结束
                if(this.batchNo){
                    // 定时器刷新文件状态
                    self.refreshStatus();
                    let fileData = new FormData();
                    for (var i = 0; i < files.length; i++)
                    {
                        // 封装用于提交上传服务器的文件信息
                        fileData.append("file", files[i]);
                        if ((i != 0 && i % 50 == 0) || i == files.length - 1)
                        {
                            self.saveUploadFiles(fileData);
                            fileData = new FormData();
                        }
                    }
                }else{
                    this.setFileStatusFailure(files);
                    D.showMsg("生成批次号失败。");
                }
                
                
            },
            generateBatchNo(){
                let self = this;
                self.$http.get('/project/upload/generateBatchNo.json？batchNo='+self.batchNo,{cache:false,
                    async:false}).then(function(res){
                    if (res && res.data && res.data.head && res.data.head.flag)
                    {
                            self.batchNo = res.data.body.message;
                    }else{
                        D.showMsg("生成批次号失败。");
                    }
                });
            },
            // generateBatchNo(files)
            // {
            //     let self = this;
            //     self.$http.get('/project/upload/generateBatchNo.json？batchNo='+self.batchNo,{cache:false,
            //         async:false}).then(function(res){
            //        if (res)
            //        {
            //            var res = res.data;
            //            if (res)
            //            {
            //                var head = res.head;
            //                var body = res.body;
            //                if (head && head.flag)
            //                {
            //                    if (body && body.message)
            //                    {
            //                        self.batchNo = body.message;
            //                        // 定时器刷新文件状态
            //                        self.refreshStatus();
                                   
            //                        let fileData = new FormData();
            //                        for (var i = 0; i < files.length; i++)
            //                     {
            //                         // 封装用于提交上传服务器的文件信息
            //                         fileData.append("file", files[i]);
            //                         if ((i != 0 && i % 50 == 0) || i == files.length - 1)
            //                         {
            //                             self.saveUploadFiles(fileData);
            //                             fileData = new FormData();
            //                         }
            //                     }
            //                    }
            //                }
            //                else
            //                {
            //                    self.setFileStatusFailure(files);
            //                    D.showMsg("生成批次号失败。");
            //                }
            //            }
            //        }
            //     })
            // },
            setFileStatusFailure(fileData)
            {
                let self = this;
                self.fileList.forEach(function(item){
                    fileData.forEach(function(file){
                        if (file.name == item.fileName && file.size == item.fileSize)
                        {
                            self.uploadingCount--;
                            self.failureCount++;
                            // 设置上传文件为失败状态
                            item.status = 2;
                        }
                     })
                })
                self.fileItems = self.fileList;
            },
           saveUploadFiles(fileData)
           {
               let self = this;
                
                self.$http.post('/project/upload/saveUploadFiles.json?batchNo=' + self.batchNo,fileData,{
                        headers:{
                            "Content-Type": "multipart/form-data"
                        }
                }).then(function(res){
                    if (res)
                    {
                        var res = res.data;
                        if (res)
                        {
                            var head = res.head;
                            var body = res.body;
                            if (head && head.flag)
                            {
                                if (body && body.message)
                                {
                                    var array = [];
                                    
                                    var resultList = body.message.fileList;
                                    
                                    self.fileList.forEach(function(item){
                                        resultList.forEach(function(file){
                                            if (file.fileName == item.fileName && file.fileSize == item.fileSize)
                                            {
                                                item.id = file.id;
                                                item.status = file.status;
                                                item.batchNo = file.batchNo;
                                                item.fileType = file.fileType;
                                                item.fileTypeName = file.fileTypeName;
                                            }
                                        })
                                    })
                                    
                                    //self.batchNo = body.message.batchNo;
                                    self.flag = true;
                                    // 保存批量上传文件到临时表后，上传文件到NAS临时路径
                                    self.uploadFiles(fileData);
                                }
                            }
                            else
                            {
                                self.setFileStatusFailure(fileData);
                                D.showMsg(head.msg);
                            }
                        }
                        else
                        {
                            self.setFileStatusFailure(fileData);
                            D.showMsg();
                        }
                    }
                    else
                    {
                        self.setFileStatusFailure(fileData);
                        D.showMsg();
                    }
               })  
           },
           refreshStatus()
           {
               let self = this;
               setTimeout(function(){
                   var i = setInterval(function(){
                       self.taskId = i;
                       self.$http.get('/project/upload/refreshData.json?batchNo=' + self.batchNo).then(function(res){
                           if (res)
                           {
                               var res = res.data;
                               if (res)
                               {
                                   var head = res.head;
                                   var body = res.body;
                                   if (head && head.flag)
                                   {
                                       if (body && body.message)
                                       {
                                           self.failureCount = body.message.failureCnt ? body.message.failureCnt : 0;
                                           self.successCount = body.message.successCnt ? body.message.successCnt : 0;
                                           self.uploadingCount = body.message.uploadingCnt ? body.message.uploadingCnt : 0;
                                           self.fileInfoList = body.message.fileInfoList;
                                           for (var i in self.fileList)
                                           {
                                               for (var j in self.fileInfoList)
                                               {
                                                   if (self.fileList[i].id == self.fileInfoList[j].id)
                                                   {
                                                       self.fileList[i].status = self.fileInfoList[j].status;
                                                   }
                                               }
                                           }
                                           self.fileItems = self.fileList;
                                       }
                                   }
                               }
                           }
                           var count = self.successCount + self.failureCount;
                           if (count == self.fileList.length)
                           {
                               //取消定时请求
                               clearInterval(self.taskId);
                           }
                       })   
                   }, 2000);
                 
               }, 2000);
           },
           uploadFiles(fileData)
           {
               let self = this;
               self.$http.post('/project/upload/uploadFiles.json?batchNo=' + self.batchNo,fileData,{
                   headers:{
                       "Content-Type": "multipart/form-data"
                   }
               }).then(function(res){
                   if (res)
                   {
                       var res = res.data;
                       if (res)
                       {
                           var head = res.head;
                           if (head && head.flag)
                           {
                               var body = res.body;
                               if (body && body.message)
                               {
                                   self.multiFileList = body.message;
                                   for (var i in self.fileList)
                                   {
                                       var fileName = self.fileList[i].fileName;
                                       for (var j in self.multiFileList)
                                       {
                                           if (self.fileList[i].fileName == self.multiFileList[j].FILE_NAME
                                                   && self.fileList[i].fileSize == self.multiFileList[j].FILE_SIZE)
                                           {
                                               self.fileList[i].BUCKET_NAME = self.multiFileList[j].BUCKET_NAME;
                                               self.fileList[i].FILE_NAME = self.multiFileList[j].FILE_NAME;
                                               self.fileList[i].FILE_SIZE = self.multiFileList[j].FILE_SIZE;
                                               self.fileList[i].SOURCE_KEY = self.multiFileList[j].SOURCE_KEY;
                                               //self.fileList[i].author = self.multiFileList[j].author;
                                               self.$set(self.fileList[i],'showlabel',true);
                                               self.$set(self.fileList[i],'keyword','');
                                           }
                                       }
                                   }
                                   self.fileItems = self.fileList;
                               }
                           }
                           else
                           {
                               self.setFileStatusFailure(fileData);
                               D.showMsg("批量上传文件失败。");
                           }
                       }
                       else
                       {
                           self.setFileStatusFailure(fileData);
                           D.showMsg();
                       }
                   }
                   else
                   {
                       self.setFileStatusFailure(fileData);
                       D.showMsg();
                   }
               })  
           },
           // 保存
           save(){
                let self = this;
                var map = {};
                map.projectId = self.projectId;
                map.dirId = self.dirId;
                map.dirName=self.dirName;
                map.files = self.fileList;
                if (map.files.length == 0)
                {
                    D.showMsg("请上传文件。");
                    return;
                }
                self.fileList.forEach(function(item){
                    var labelIds = item.idList.join(";");
                    self.$set(item,'labelIds',labelIds);
                    var labelNames = item.labels.join(";");
                    self.$set(item,'labelNames',labelNames);
                })

                self.$http.post('/project/upload/createDocuments.json',map).then(function(res){
                    if (res)
                    {
                        var res = res.data;
                        if (res)
                        {
                            var head = res.head;
                            var body = res.body;
                            if (head && head.flag)
                            {
                                // ES缓存每1秒刷新一遍，以下代码主要是为了等ES缓存刷新完毕后再调列表查询接口  by lwx506570
                                D.block();
                                setTimeout(function(){
                                    D.unblock();
                                    self.fileItems = [];
                                    self.uploadFilesCount = 0;
                                    self.successCount = 0;
                                    self.items = [];
                                    self.$emit("getDirectoryDoc",self.directoryItem);
                                    self.relatedDocDialog = false;
                                },3000);
                                           
                                
                            }
                            else if (head && !head.flag)
                            {
                                var errorCode = head.errorcode;
                                if (errorCode && "dfx.project.file.uploading" == errorCode)
                                {
                                    D.showMsg("部分文件上传中，请等待全部文件上传完成后，再提交保存。");
                                }
                                else if (errorCode && "dfx.project.upload.temp.failed" == errorCode)
                                {
                                    D.showMsg("上传成功的文件集合为空，提交保存失败。");
                                }
                                else
                                {
                                    D.showMsg();
                                }
                            }
                        }
                        else
                        {
                            D.showMsg();
                        }
                    }
                    else
                    {
                        D.showMsg();
                    }
                })
            },
            reset(){
                if (this.$refs.folder)
                {
                    this.$refs.folder.value = "";
                }
                if (this.$refs.file)
                {
                    this.$refs.file.value = "";
                }
                this.fileItems = [];
                this.fileList = [];
                this.batchNo = "",
                this.uploadFilesCount = 0;
                this.successCount = 0;
                this.failureCount = 0;
                this.uploadingCount = 0;
                this.items = [];
            },
            // 联想查询标签
            querySearch(queryString, cb) {
                let strLen = queryString.split(';').length;
                let queryStr = queryString.split(';')[strLen - 1];

                if(queryStr){
                    let self = this;
                    this.$http.post('/project/document/documentLabel/list/page.json?offset=0&limit=30',
                    {   
                        "status":"1",
                        "labelType":"1",
                        "labelName":queryStr
                    }).then(function(res){
                        if(res.data.head.flag){
                            var retLabelList=[];
                            if(res.data.body.documentLabelList){
                                for(var i=0;i<res.data.body.documentLabelList.length;i++){
                                    var item={};
                                    item.value=res.data.body.documentLabelList[i].labelName;
                                    retLabelList.push(item);
                                }
                                // 展示在下拉列表上
                                cb(retLabelList);
                            }
                        };
                    }).catch(e=>{console.log(e);});
                }else{cb(null)};  
            },
            // 按回车键后
            keydownEvent(event,item){
                var keyVal = event.keyCode;//键值
                // 回车键
                debugger;
                if(keyVal == 13){
                    var reg = /^[^*\/|:<>?,\\"]*$/;
                    let len = item.keyword.split(';').length;
                        item.keyword = item.keyword.split(';')[len - 1 ];
                    if (!item.keyword) {
                        D.showMsg("请输入内容")
                        return;
                    };
                    if(!item.keyword || !reg.test(item.keyword)){
                        D.showMsg("不能包含以下字符 *  : ; ? , &quot; &lt; &gt; | \ /")
                        return;
                    }
                    if(item.keyword.length > 20){
                        D.showMsg("标签长度不能超过20")
                        return;
                    }
                    // 去重
                    // item.label += ';';
                    var label = item.keyword ;
                    if(label){
                        for(var i = 0; i < item.labels.length; i++){
                            if(label == item.labels[i]){
                                return;
                            }
                        }
                        if(item.labels.length > 4){
                            D.showMsg("标签最多为5个。");
                            return;
                        }
                        item.labels.push(label);
                        // item.label = "";
                        console.log("end====",this.fileItems)
                        item.showlabel = true;
                        item.keyword='';
                    }
                }
            },
            // 选中下拉框后
            handleSelect(event,item){
                debugger;
                console.log(this.fileItems);
                // if(event.keyCode){

                // }
                var value = event.value;
                if(value){
                    for(var i = 0; i < item.labels.length; i++){
                        if(value == item.labels[i]){
                            item.keywords = '';
                            return;
                        }
                    }
                    if(item.labels.length > 4){
                        D.showMsg("标签最多为5个。");
                        return;
                    }
                    // item.label += ';'; 
                    item.showlabel = true;
                    //item.keyword='';
                    // event.value += ';';
                    item.labels.push(event.value );
                    return;
                    // this.keywords = '';
                }
                // console.log("this.$refs.autocomplete:",this.$refs.autocomplete);
            },

            //清空标签
            Blur(event,item){
                let self = this;
                setTimeout(function(){
                    //self.$refs.autocomplete.suggestions = [];
                    if(self.$refs.autocomplete){
                        self.$refs.autocomplete.suggestions = [];
                    }
                    if(item.labels.length > 0 ){
                        item.showlabel = true;
                        //item.keyword='';
                    }
                }, 300);
            },
        },
    }
</script>
<!--batchImportEnd-->
<!--docListStart-->
<template>
    <div class="Prodocument">
        <div class="mainContent">
            <div class="directory left" id="docNav">
                <div class="top" >
                    <span  class="title left">项目文档库目录</span>
                    <div v-if="authorized==true" class="configurationName cursor" @click="configuration()">
                        <span class="right " v-show="project.status=='0'">配置</span>
                        <span class="vertical right op_btn" style="padding-left:" v-show="project.status=='0'">
                            <img  class="configurationImg right" :src="configurationImg">
                        </span>
                    </div>
                </div>
                <div class="buttom">
                    <template v-for="item in directoryList">
                        <div class="directoryRoot cursor"  :class="[item.id==directoryId?'clickColor':'']">
                            <span v-if ="item.isOpen=='true'" @click="expandCollapse(item)" class="img left imgOpen"></span>
                            <span v-else @click="expandCollapse(item)" class="img left imgClose"></span>
                            <span @click="getNodes(item)" class="left name bubble" :title="item.name">{{item.name}}</span>
                        </div>
                        <div v-if="item.subDirectoryList!=null && item.subDirectoryList.length>0 && item.isOpen=='true' " style="width:100%">
                                <directoryRecursion :subDirectoryList = "item.subDirectoryList" :level='1' :directoryId='directoryId'
                                     @expandCollapse="expandCollapse" @getNodes="getNodes" >
                                </directoryRecursion>  
                        </div>
                    </template>
                </div>
                <div class="verticalLine" id="verLine" @mousedown="mousedown($event)"></div>
            </div>
            
            <div class="docList right" id="docContent">
                <div class="top">
                    <div class="left operateBtn" v-show="project.status=='0'">
                        <div class="importBtn left cursor" @click="batchImport()" v-if="projectMemberFlag">
                            <span class="vertical op_btn">
                                <img  class="left" :src="importDoc"></img>
                            </span>
                            <span class="right" >批量导入</span>
                        </div>
                        
                        <div class="deleteBtn left cursor " :class="{'unOption':isOption}" @click="delDoc()" v-if="projectMemberFlag">
                            <span class="vertical op_btn">
                                <img  class="left" :src="deleteBtn" style="padding-left:7px;"></img>
                            </span>
                            <span class="right">删除</span>
                        </div>

                        <div class="deleteBtn tab left cursor" :class="{'unOption':isOption}" @click="batchSetLabel()" v-if="projectMemberFlag">
                            <span class="vertical op_btn">
                                <img  class="left" src="../../assets/setting.png" style="padding-left:4px;padding-top:4px"></img>
                            </span>
                            <span class="right">批量设置标签</span>
                        </div>
                        <div class="deleteBtn tab left cursor" :class="{'unOption':isOption}" @click="batchMove()" v-if="projectMemberFlag">
                            <span class="vertical op_btn">
                                <img  class="left" src="../../assets/batch_move.png" style="padding-left:4px;padding-top:4px"></img>
                            </span>
                            <span class="right">批量移动</span>
                        </div>
                    </div>
                    <div class="right">
                        <div class="search">
                            <img class="searchreset" src="../../assets/project/clear.png" @click = "reset" id="resetimg" :style="{display:resetimg}" v-if="isedit"></img>
                            <img class="searchbtn" src="../../assets/project/search.png" @click = "search"></img>
                            <input type = "text" placeholder = "" v-model = "searchKeyWord" @keyup.enter = "search" 
                                   onfocus="this.placeholder=''"
                                   v-on:input="isreset">
                        </div>
                    </div>
                </div>
                <div class="table buttom">
                    <el-table
                        :data="docList"
                        style="width: 100%"
                        empty-text=" "
                        max-height="750"
                        @sort-change="sortColumn"
                        @selection-change="handleSelectionChange"
                        border>
                        <el-table-column 
                          :selectable="selectable"
                          type="selection"
                          width="55">
                        </el-table-column>
                        <el-table-column label="文档标题">
                            <template slot-scope="scope">
                                <el-popover trigger="hover" placement="right-end">
                                    <p>{{ scope.row.name }}</p>
                                    <div slot="reference" class="name-wrapper">
                                        <span>
                                            <a :href="'/browse/onLineBrowse/getBrowseUrl.json?nodeId='+scope.row.id" target="_blank">{{ scope.row.name}}</a>
                                        </span>
                                    </div>
                                </el-popover>
                            </template>
                        </el-table-column>
                        <el-table-column label="类型">
                            <template slot-scope="scope">
                                <el-popover trigger="hover" placement="right-end">
                                    <p>{{ scope.row.fileType }}</p>
                                    <div slot="reference" class="name-wrapper status_td">
                                        <span>{{ scope.row.fileType}}</span>
                                    </div>
                                </el-popover>  
                            </template>  
                        </el-table-column>
                        <el-table-column label="文档作者">
                            <template slot-scope="scope">
                            	<el-popover trigger="hover" placement="right-end">
                                    <p>{{ scope.row.authorCommonName }}</p>
                                    <div slot="reference" class="name-wrapper">
                                        <span>{{ scope.row.authorCommonName}}</span>
                                    </div>
                                </el-popover>
                                    <!-- espaceGroup :item="scope.row" :sysUserId="scope.row.createBy" :userName="scope.row.authorCommonName" :title="''" :espaceMoreClass="'listUserNameCss'"></espaceGroup -->
                            </template>
                        </el-table-column>
                        <el-table-column label="标签">
                            <template slot-scope="scope">
                                <el-popover trigger="hover" placement="right-end">
                                    <p>{{ scope.row.keywords }}</p>
                                    <div slot="reference" class="name-wrapper">
                                        {{ scope.row.keywords}}
                                    </div>
                                </el-popover>
                            </template>
                        </el-table-column>
                        <el-table-column label="发布时间" sortable="custom" prop="publishTime">
                            <template slot-scope="scope">
                                <div slot="reference" class="name-wrapper">
                                    <span>{{ scope.row.publishTime}}</span>
                                </div>
                            </template>   
                        </el-table-column>  
                    </el-table>  
                    <pagination :total = "total" :pageItem="pageItem" @pagechange = "pagechange" ref = "pag"></pagination>
                </div>
            </div>
        </div>
        <configdocNav ref="configdocNav" :pId="projectId" :directoryId="RootDirId" v-on:refreshNav="getResult"></configdocNav>
        <batchImport ref='batchImport'  @getDirectoryDoc="getDirectoryDoc"></batchImport>
        <batchSetLabelModal ref='batchSetLabelModal' @pagechange = "pagechange"></batchSetLabelModal>
        <batchMove ref="batchMove" :indexList="indexList" :seleList="seleList" @resetList='search'></batchMove>
    </div>
</template>
<script>
    import batchMove from '../assemblies/batchMove.vue'
    import batchSetLabelModal from '../project/batchSetLabelModal.vue'
    import directoryRecursion from '../project/directoryRecursion.vue'
    import configdocNav from '../assemblies/configdocNav.vue'
    import batchImport from '../project/batchImport.vue'
    export default{
        components:{directoryRecursion,configdocNav,batchImport,batchSetLabelModal, batchMove},
        props:{
            project:{
                type:Object,
                default:{}
            }
        },
         //状态转换
        filters:{
            turnStatus: function (value) {
                switch(value){
                    case "3": return "扫描中";
                    case "1": return "已发布";
                    case "4": return "发布失败";
                    default:
                        return "";
                };
            }
        },  
        data(){

            return {
                project:{},
                seleList:[],//选中项
                supId:'',//选中目录用于查目录
                sort:'desc',//排序方向
                projectId:this.$route.query.projectId,
                directoryId:"",//获取目录查文档(选中目录id)
                docList:[],
                directoryList:[],
                configurationImg:require("../../assets/config.png"),
                addTree:require("../../assets/add-tree.png"),
                importDoc:require("../../assets/project/Import.png"),//批量导入
                deleteBtn:require("../../assets/project/delete-icon-hover.png"),//批量删除
                setting:require("../../assets/project/setting.png"),//设置批量标签
                treeCloseIcon:require("../../assets/project/tree-close-icon.png"),//树收缩图片
                treeOpenIcon:require("../../assets/project/tree-open-icon.png"),//树展开图片
                uploadImg:require("../../assets/1.png"),//上传中
                successImg:require("../../assets/2.png"),//成功
                errorImg:require("../../assets/fail.png"),//失败
                isedit:false,//编辑中状态
                resetimg:'none',//编辑小叉叉按钮
                searchKeyWord:'',
                total:0,
                pageItem:[{text:"20"},{text:"50"},{text:"100"}],
                authorized:false,
                // projectId:"PRO1000000028",//projectID
                obj:{
                    offset:0,
                    limit:20
                },//分页初始值
                load:'no',//是否加载
                initDocNav:[],//初始导航树一级目录
                // 根目录Id
                RootDirId:"",
                hasPermission:false,//是否有查看文档权限
                fileList:[],
                nodeType:'',
                drag:false,
                docNavWidth:300,
                navWidth:'',
                mouseStart:{},
                orderByClickNum:0,//点击排序次数
                indexList:[],//目录数据
                projectMemberFlag:false,//是否为项目成员
                isOption:true,
            }
        },
        created:function(){
            this.getProject();
            this.isProjectMember();
            this.isAdminOrOwner();
            this.increaseCount(this.projectId);
            this.$emit('initData');
        },   
       watch:{
    	   seleList:function(val){
    		   if(!val ||val.length==0){
    			   this.isOption=true;
    		   }else{
    			   this.isOption=false;
    		   }
    	   }
       },
        methods:{
            selectable(index,row){
                let self = this;
                var author = index.authorCommonName;
                if(self.authorized){
                    return true;
                }
                if(author == D.commonName){
                    return true;
                }else{
                    return false;
                }   
            },
            //批量移动文档
            batchMove(){
                // console.log("this.seleList ", this.seleList)
                if(this.seleList && this.seleList.length > 0){
                // 先清空组件的数据
                // this.$refs.batchSetLabelModal.lables = [];
                // this.$refs.batchSetLabelModal.idList = [];
                // this.$refs.batchSetLabelModal.keywords = "";
                // this.$refs.batchSetLabelModal.nodeList = this.seleList;


                    this.indexList = this.directoryList;
                    this.$refs.batchMove.itemData =[];
                    this.$refs.batchMove.docDialog = true;
                    this.$refs.batchMove.selectName ='';
                    this.$refs.batchMove.selectIndexId ='';
                    console.log("点击打开弹窗的数据",this.indexList);
                    if(this.indexList.length){
                        this.indexList.forEach(function(item){
                            item.checked = false;
                            item.expandNo = "";
                            item.open = false;
                        });
                        this.$refs.batchMove.itemData = JSON.parse(JSON.stringify(this.indexList));
                        this.$refs.batchMove.projectId=this.projectId;
                        this.$store.state.moveIndex.MoveIndex_Arr = JSON.parse(JSON.stringify(this.indexList));
                    }else{
                        this.$refs.batchMove.itemData = [];
                        this.$store.state.moveIndex.MoveIndex_Arr = [];
                    }

                }else{
                    //D.showMsg("请选择文档");
                }
            },
             //批量设置标签弹窗
            batchSetLabel(){
                if(this.seleList && this.seleList.length > 0){
                // 先清空组件的数据
                this.$refs.batchSetLabelModal.lables = [];
                this.$refs.batchSetLabelModal.idList = [];
                this.$refs.batchSetLabelModal.keywords = "";
                this.$refs.batchSetLabelModal.relatedDocDialog = true;
                this.$refs.batchSetLabelModal.nodeList = this.seleList;
                this.$refs.batchSetLabelModal.projectId = this.projectId;

                }else{
                    //D.showMsg("请选择文档");
                }
            },
             //是否是项目成员
            isProjectMember(){
                var self=this;
                D.block();
                self.$http.get("/project/member/getRole.json?projectId="+self.projectId).then(res=>{
                    D.unblock();
                    if(res && res.data && res.data.flag){
                        //roleType为2则是非项目成员
                        if(res.data.roleType!=2){
                            self.projectMemberFlag =true;
                        }
                    }else{
                        D.showMsg();
                    }    
                }).catch(function(){
                    D.unblock();
                    D.showMsg();
                });  
                
            },
            //是否项目管理员
            isAdminOrOwner(){
                var self=this;
                D.block();
                self.$http.get("/project/member/isAdminOrOwner.json?projectId="+self.projectId).then(res=>{
                    if(res){
                        self.authorized = res.data;
                    }else{
                        D.unblock();
                        D.showMsg();
                    }    
                }).catch(function(){
                    D.unblock();
                    D.showMsg();
                });  
            },
           
            //获取项目根目录id
            getProject(){
                var self = this;
                self.$http.get('/project/project/getProjectBasicById.json'+new Date().getTime()+'?projectId='+self.projectId).then(res => {
                    if(res && res.data &&res.data.body && res.data.body.projectBasicInfo &&res.data.head && res.data.head.flag){
                        self.directoryId=res.data.body.projectBasicInfo.directoryId;
                        self.RootDirId= res.data.body.projectBasicInfo.directoryId;
                        self.getResult(self.RootDirId);
                    }else{
                        D.unblock();
                        D.showMsg();
                    }

                }).catch(function(){
                    D.unblock();
                    D.showMsg();
                });  
            },
            //初始化文档
            pagechange(obj){
                if(this.load=='no'){
                    return;
                }
                this.getDoc(obj);
            },
            //获取文档
            getDoc(obj){
                var self=this;
                var offset=obj.offset || "0";
                var limit=obj.limit || "20";
                var pDocumentQueryDto = {
                    projectId: self.projectId,
                    naviid: self.directoryId,
                    name: self.searchKeyWord,
                    sortBy: null,
                    pageIndex: offset,
                    pageSize: limit
                }
                D.block();
                // self.$http.post("/project/document/getDocumentList.json?directoryId="+self.directoryId
                //     +"&searchKeyWord="+encodeURIComponent(self.searchKeyWord)
                //     +"&sort="+self.sort+"&offset="+offset+"&limit="+limit
                //     ).then(res=>{
                self.$http.post("/project/document/getDocumentList.json?sortOrder="+self.sort,pDocumentQueryDto,{
                    cache:false,
                    async:false,
                    contentType:'application/json;charset=UTF-8'
                }).then(res=>{
                    if(res && res.data && res.data.body && res.data.head && res.data.head.flag){
                        D.unblock();
                        self.total=res.data.body.count;
                        self.docList=res.data.body.pDocumens;
                        // if(self.docList){
                        //     self.docList.forEach(function(item){
                        //         if(item.fieldValues && item.fieldValues.fileType){
                        //             var fileType=item.fieldValues.fileType.values[0];
                        //             self.$set(item,'fileType',fileType);
                        //         }else{
                        //             self.$set(item,'fileType',"其它");
                        //         }
                        //     });
                        // }
                    }else{
                        D.unblock();
                        D.showMsg();
                    }    
                }).catch(function(){
                    D.unblock();
                    D.showMsg();
                });  
            },
            //递归第一次加载数据
             recursion(obj){
                var self=this;
                if(obj){
                    obj.forEach(function(item,index){
                        if(item.subDirectoryList && item.subDirectoryList.length>0){
                            item.isOpen='true';
                            self.recursion(item.subDirectoryList);
                        }else{
                            //是否有子节点（第一次加载树结构的前三层，最后一级子节点subDirectoryList数据都为null，
                            // 加入hasChildren判断该节点是否有子节点数据）
                            if(item.hasChildren){
                                item.isOpen='false';
                            }else{
                                item.isOpen='false';
                            }
                        }
                        
                    });
                }else{
                    return;
                }
            },

            //获取根目录
            getResult(directoryId){
                var self=this;
                D.block();
                self.$http.post("/project/directory/list/page.json",{
                    "supId":directoryId,
                }).then(res=>{
                    if(res && res.data && res.data.body && res.data.head && res.data.head.flag){
                            D.unblock();
                            self.directoryList=res.data.body.directoryList;
                            self.indexList = self.directoryList;
                            self.initDocNav = JSON.parse(JSON.stringify(self.directoryList));
                            self.recursion(self.directoryList,1);
                            
                        }else{
                            D.unblock();
                            D.showMsg();
                        }    
                }).catch(function(){
                    D.unblock();
                    D.showMsg();
                });     
            },
            //重置搜索
            reset(){
                let self = this;
                self.searchKeyWord='';
                self.isedit = false;
            },

            //搜索按钮
            search(){
                let self = this;
                self.getDoc(self.obj);

            },

            //是否展示小叉图片
            isreset(){
                if(this.searchKeyWord.trim().length > 0){
                    this.resetimg = "inline-block";
                    this.isedit = true;
                }
                else{
                    this.isedit = false;
                }
            },

            //排序
            sortColumn(column){
                var self=this;
                self.orderByClickNum++;
                if(self.orderByClickNum % 2 == 0){
                    self.sort="desc";
                }else{
                    self.sort="asc";
                }
                self.getDoc(self.obj);

                
            },
            //选中值
            handleSelectionChange(val) {
                this.seleList = val;
            },

            //批量导入
            batchImport(){
                if(this.directoryId && this.directoryId!= this.RootDirId){
                    if(!this.hasPermission){
                         D.showMsg("没有权限！");
                         return;
                    }
                    this.$refs.batchImport.relatedDocDialog = true;
                    this.$refs.batchImport.reset();
                    this.$refs.batchImport.generateBatchNo();
                }else{
                    D.showMsg("请选择目录节点");
                }
            },
            //删除文档
            delDoc(){
                var self=this;
                var flag=true;
                if(self.seleList && self.seleList.length==0){
                    //D.showMsg("请选择要删除的文档！");
                    return;
                }
                if(!self.authorized){
                    if(self.seleList){
                        self.seleList.forEach(function(item){
                            if(item.authorCommonName!=D.commonName){
                                flag=false;
                                return;
                            }
                        });
                    }
                } 
                if(!flag){
                    D.showMsg("处理失败,只有管理员和文档作者才能删除");
                    return;
                }   
                D.showMsg('确定删除吗？',function(){
                    //如果不是管理员，又不是文档导入人，则不允许删除
                    D.block();
                    self.$http.post("/project/document/deleteDoucument.json?projectId="+self.projectId,self.seleList)
                      .then(res=>{
                          if(res&&res.data&&res.data.head&&res.data.head.flag){
                                setTimeout(function(){
                                    D.unblock();
                                    self.getDoc(self.obj);
                                },3000);
                            }else{
                                D.unblock();
                                D.showMsg();
                            }
                      }).catch(function(){
                          D.unblock();
                          D.showMsg();
                      });    
                },true,true);

            },

            //配置
            configuration(){
                this.$refs.configdocNav.showConfigIndex = true;
                this.$refs.configdocNav.rootDirId = this.RootDirId;
                this.$refs.configdocNav.projectCategory = this.project.projectCategory;
                if(this.initDocNav.length){
                    this.initDocNav.forEach((item,index)=>{
                        item.weight = index;
                    })
                    this.$store.state.docNav.project_Arr = JSON.parse(JSON.stringify(this.initDocNav));
                    this.$refs.configdocNav.itemData = JSON.parse(JSON.stringify(this.initDocNav));   
                }else{
                    this.$refs.configdocNav.itemData = [];
                    this.$store.state.docNav.project_Arr = [];
                    this.$refs.configdocNav.getIndex();
                }

            },

            //展开收拢
            expandCollapse(item){
                var self=this;
                self.supId=item.id;
                //设置加载文档
                self.load="yes";
                //设置展开收拢状态(如果点击过当前目录只需要重新加载文档数据)
                if(item.isOpen=="true"){
                    self.$set(item,'isOpen','false');
                   
                }else if(item.isOpen== "false"){
                    //展开
                    self.$set(item,'isOpen','true');
                    
                }
                //接口逻辑父级目录的附加不为null时，并且有权限的情况下，下面所有子目录都继承权限
                //hasPermission为false，接口判断子级别目录是否有权限，为true子级目录继承权限
                //内部公开则directoryProperty为null
                var hasPermission = false;//父目录是否有权限
                if(item.directoryProperty != null && item.directoryProperty.length > 0){
                    hasPermission = item.hasPermission;
                }
                //如果是继承
                if(item.inheritance){
                    hasPermission = true;
                }

                D.block();
                self.$http.post("/project/directory/list/page.json",{
                    "supId":self.supId,
                    "hasPermission":hasPermission,
                    "level":1
                }).then(res=>{
                    if(res && res.data && res.data.body && res.data.head && res.data.head.flag){
                        D.unblock();
                        self.$set(item,'subDirectoryList',res.data.body.directoryList);
                        //设置子目录默认为收缩状态
                        item.subDirectoryList.forEach(function(obj){
                            //是否有子节点
                            if(obj.hasChildren){
                                self.$set(obj,'isOpen','false');
                            }else{
                                self.$set(obj,'isOpen','true');
                            }
                            
                            //如果hasPermission为true，则即表示继承权限
                            if(hasPermission){
                                self.$set(obj,'inheritance',true);
                            }
                        });
                    }else{
                        D.unblock();
                        D.showMsg();
                    }    
                }).catch(function(){
                    D.unblock();
                    D.showMsg();
                });  
                
            },
            //点击目录查询文档
            getNodes(item){
                var self=this;
                //第一次加载文档（不通过分页调用）后设置load为yes
                self.load="yes";
                self.directoryId=item.id;
                self.$refs.batchImport.directoryItem = item;
                self.$refs.batchImport.dirId = item.id;
                self.$refs.batchImport.dirName = item.name;
                D.block();
                this.$http.post("/project/directory/hasPermission.json?projectId="+self.projectId,{
                    "id":self.directoryId
                }).then(res=>{
                    if(res && res.data &&  res.data.head && res.data.head.flag){
                        D.unblock();
                        self.getDoc(self.obj);
                        self.hasPermission=true;
                    }else{
                        D.unblock();
                        self.hasPermission=false;
                        //self.getDoc(self.obj);
                        self.docList = [];
                    }
                }).catch(function(){
                    D.unblock();
                    D.showMsg();
                });  
            },
            //获取浏览
            getBrowse(item){
                let self = this,
                browseUrl='';
                this.$http.get('/onlinebrowse/onLineBrowse/getBrowseUrl.json?nodeId='+item.id,{
                }).then(function(res){
                    res = res.data;
                    if(res.head.flag){
                        browseUrl = res.body.browseUrl;
                        window.open(browseUrl);
                    }else{
                        D.showMsg()
                    }
                }).catch(e=>{}); 

            },
            increaseCount(projectId){
                this.$http.get('/project/project/increaseCount.json'+new Date().getTime()+'?projectId='+projectId)
            },
            getDirectoryDoc(item){
                // this.expandCollapse(item);
                this.getNodes(item);
            },
            mousedown(ev){
                window.addEventListener('mouseup', this.stopDrag);
                window.addEventListener('mousemove', this.doDraging);
                var oEvent=ev||event;
                this.mouseStart.x=oEvent.clientX;
                this.mouseStart.y=oEvent.clientY;
                this.navWidth = document.getElementById('docNav').offsetWidth;
                this.drag = true;
            },
            //拖动ing
            doDraging(ev){
                if(this.drag){
                    this.positionCalc(ev);
                }
            },
            //停止拖动
            stopDrag(ev){
                if(this.drag){
                    this.drag = false    
                    this.positionCalc(ev);
                }
            },
            positionCalc(ev){
                var oEvent=ev||event;
                let nw = oEvent.clientX - this.mouseStart.x;
                if(nw > 0){
                    if(nw + this.docNavWidth >=1000 ){
                        nw = 1000; 
                    }else{
                        nw = nw + this.docNavWidth;
                    }
                }else{
                    nw = this.navWidth + nw
                    if(nw <= this.docNavWidth){
                        nw =  this.docNavWidth; 
                    }
                }
                document.getElementById('docNav').style.width = nw +'px';
                document.getElementById('docContent').style.width = 'calc(100% - '+nw +'px)';  
            }


        },
    }
</script>
<!--docListEnd-->
<!--taskStart-->
<template>
    <div class="addtask project clearBox">
        <div class="breadcrumbs">
            <a href="/myspace/homepage" class="bread_a">个人空间</a>
            <span>/</span>
            <a href="/myspace/project/list" class="bread_a">项目管理</a>
            <span>/</span>
            <span>{{titleName}}</span>
        </div>
        <!--基础信息-->
        <div class="taskCont clearBox">
            <h2 class="title">{{titleName}}</h2>
            <h4 class="menu_h4">基本信息配置</h4>
            <div class="half_row fl">
                <label class="left fl required labelColor">任务名称:</label>
                <div class="right fl ">
                    <input placeholder="必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; | \ /" 
                        type="text"  
                        maxlength="200" 
                        autofocus="autofocus"
                        v-model.trim="title" 
                        onfocus="this.placeholder=''" 
                        onblur="placeholder='必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; \| \\ \/'"
                        @blur="titleBlur"
                        :class="{'error_border':isTitle}" >
                    <div class="validBox" v-show="isTitle">
                        <span class="line"></span>
                        <span class="fill"></span>
                    必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; | \ /</div>
                </div>  
            </div>

            <div class="half_row fl">
                <label class="left fl labelColor">计划完成时间:</label>
                <div class="right fl">
                    <div class="block" >
                        <el-date-picker 
                          v-model="planTime"
                          type="date"
                          placeholder="选择日期"
                          format="yyyy-MM-dd"
                          value-format="yyyy-MM-dd"
                          :picker-options="pickerLimit"
                          :editable="false"
                          :class="{'errorBorder':isPlanTime}"
                          @change="changePick">
                        </el-date-picker>
                    </div>
                    <div class="validBox" v-show="isPlanTime">
                        <span class="line"></span>
                        <span class="fill"></span>
                    必填，请选择计划完成时间</div>
                </div>  
            </div>        

            <div class="row fl">
                <label class="left fl labelColor">责任人:</label>
                <div class="right fl" v-show="this.projectCategory == '1'">
                    <div class="select_div" style="width:100%;">
                        <input 
                                disabled="disabled"
                                style="width:92.5%"
                                type="text"
                                maxlength="200"
                                ref="author"
                                v-model.trim="owner" >
                    </div>
                </div>
                <div class="right fl" v-show="this.projectCategory != '1'">
                    <div class="select_div" >
                        <jobNumber ref="author"                           
                            :class="{'errorBorder':isOwner}"
                            style="width:92.3%"
                            type="input"  
                            :sign.sync="owner" 
                            v-on:blurEvent="ownerBlur" 
                            ></jobNumber>
                            <div class="validBox" v-show="isOwner">
                                <span class="line"></span>
                                <span class="fill"></span>
                            责任人最多输入20个以;分隔</div>
                    </div>
                </div>  
            </div>



            <div class="row fl">
                <label class="left fl labelColor">任务说明:</label>
                <div class="right fl">
                    <textarea type="text" v-model="taskDes" style="overflow-y:hidden;width:92.3%"
                    maxlength="1000"     @blur="taskdesBlur" 
                        :class="{'error_border':isTaskdes}" ></textarea>
                    <div class="validBox" v-show="isTaskdes">
                        <span class="line"></span>
                        <span class="fill"></span>
                    任务说明不能超过1000字符</div>
                </div>  
            </div>
            <h4 class="menu_h4 fl">任务模板选择</h4>
            <div class="half_row fl">
                <label class="left required fl labelColor">任务类型:</label>
                <div class="right fl">
                    <selected :list="taskTypeL"
                        :class="{'error':isTaskType,'disable':taskId}" 
                        :value.sync="taskTypeId" 
                        :name="'attributes.ZH'"
                        :sign="'id'"
                        v-on:unfold="getTaskTypeL"  
                        v-on:chose="choseTaskType" 
                        :placeholder="taskTypeName"></selected>
                    <div class="validBox" v-show="isTaskType">
                        <span class="line"></span>
                        <span class="fill"></span>
                    请选择任务类型</div>
                </div>  
            </div>

            <div class="half_row fl">
                <label class="left fl labelColor">任务模板:</label>
                <div class="right fl">
                    <selected :list="taskTempL"
                        :value.sync="taskTempId" 
                        :name="'taskTempName'"
                        :sign="'ttId'"
                        :class="{'disable':!taskTypeId||taskId}"
                        v-on:unfold="getTaskTempL"  
                        v-on:chose="choseTaskTemp"
                        ref="taskTemp" 
                        :placeholder="taskTempName"></selected>
                </div>  
            </div>
            <div class = "tasktemp_cont fl" v-show="isTaskTemp">
                <div class="tasktemp_detail_cont clearBox ">
                    <div class="tasktemp_meun" @click="isfold = !isfold">
                        <span class="fold" :class="{'unfold':isfold}"></span>
                        任务模板详情
                    </div>
                    <div v-show="isfold" class="tasktemp_detail clearBox">
                        <div class="half_row row_slt fl">
                            <label class="left fl labelColor" >模板文档:</label>
                            <div class="right fl">
                                <div class="doc_box" >
                                    <!-- <a href="javaScript:void(0);" @click="viewDoc(tempdocId)" class="inputbox" :title="tempdocName">{{tempdocName}}</a> -->
                                    <a :href="'/browse/onLineBrowse/getBrowseUrl.json?nodeId='+tempdocId" target="_blank" class="inputbox" :title="tempdocName">{{tempdocName}}</a>
                                    <span class="clear_btn" @click="cleartempdoc" v-if="tempdocName && !taskId">X</span>
                                    <span class="select_span select_knowledge nowrap"  @click="showDocDialog('0')" v-if="!taskId" title="选择知识库文档">选择知识库文档</span>
                                    <label class="select_span select_local nowrap" v-if="!taskId" for = "fileChange" title="选择本地文档">选择本地文档</label> 
                                     <input type = "file" id = "fileChange" @change = "fileChange($event)" ref = "inputer" style="display:none" accept='application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document'>
                                </div>
                                <p class="tip notNewLine">提示：模板文档只能是IDP文档，如果选择本地文档，只能选择Word文档。</p>
                            </div>
                        </div>
                        <div class="row fl"></div>
                        <div class="half_row row_slt fl">
                            <label class="left fl labelColor">设计向导:</label>
                            <div class="right fl">
                                <div class="doc_box">
                                    <!-- <a href="javaScript:void(0);" @click="viewDoc(docAssistant)" class="inputbox"  :title="docName">{{docName}}</a> -->
                                    <a :href="'/browse/onLineBrowse/getBrowseUrl.json?nodeId='+docAssistant" target="_blank" class="inputbox" :title="docName">{{docName}}</a>
                                    <span class="clear_btn" @click="cleardoc" v-if="docName">X</span>
                                    <span class="select_span select-btn" @click="showDocDialog('1')">选择</span>
                                </div>
                            </div>
                        </div>

                        <div class="row fl">
                            <label class="left fl labelColor">搜索关键字:</label>
                            <div class="right fl">
                                <input type="text" v-model="searchWords" 
                                    @blur="keywordsBlur"
                                    :class="{'error_border':iskeywords}" >
                                <div class="validBox" v-show="iskeywords">
                                    <span class="line"></span>
                                    <span class="fill"></span>
                                    搜索关键字不能超过100个字符
                                </div>
                                <p class="tip">提示：默认搜索关键字配置，可输入多个，使用空格分隔。</p>
                            </div>
                        </div>

                        <div class="row fl">
                            <label class="left fl labelColor">相关资料:</label>
                            <div class="right fl">
                                <p class="select_p"><span class="select_span" @click="showRelatedDocDialog()" :title="'最多可再选择'+mostChose+'个相关资料'">选择</span></p>
                                <div class="relInfor table" v-show="relateItems.length != 0">
                                    <el-table
                                        :data="relateItems"
                                        tooltip-effect="dark"
                                        style="width: 100%; max-height:300px;"
                                        >
                                        <el-table-column
                                          label="资料名称" width="300">
                                          <template slot-scope="scope">
                                            <el-popover trigger="hover" placement="right-end">
                                              <p>{{ scope.row.name }}</p>
                                              <div slot="reference" class="name-wrapper">
                                                <span>
                                                    <a class="infoName" href="javaScript:void(0);" @click="viewDoc(scope.row.id)">{{ scope.row.name }}</a>
                                                </span>
                                              </div>
                                            </el-popover>
                                          </template>
                                        </el-table-column>

                                        <el-table-column
                                            label="参考说明">
                                            <template slot-scope="scope">
                                                <input type="text"  maxlength='1000' v-model = "scope.row.des" />
                                            </template>
                                        </el-table-column>
                                        <el-table-column
                                            label="Operation"
                                            width="120">
                                            <template slot-scope="scope">
                                                <div class="operate_td">
                                                    <span title="删除" class="span_btn" @click="delRelatedDoc(scope.row)">
                                                        <img src="../../assets/delete-btn.png"></img></span>
                                                </div>

                                            </template>
                                        </el-table-column>
                                    </el-table>
                                        
                                </div>    
                            </div>
                        </div>

                        <div class="row fl">
                            <label class="left fl labelColor">辅助工具:</label>
                            <div class="right fl">
                                <div class="tools">
                                    <div class="domain">
                                    <span class="tool" v-for="item in domainList" @click="getTools(item.id)" v-bind:class="{'activity':domainId==item.id}">{{item.attributes && item.attributes.ZH}}
                                    </span>
                                </div>
                                <div class="checkboxall">
                                    <el-checkbox :indeterminate="isIndeterminate" v-model="ischeck" style="line-height: 20px" @change="checkTools()">全选</el-checkbox>
                                </div>
                                    <el-checkbox-group v-model="assisantTools"> 
                                        <el-checkbox v-for="item in toolList" :key="item.id" :label="item.id" @change="checkinlist(item.id)" :title="item.attributes && item.attributes.ZH">
                                            {{item.attributes && item.attributes.ZH}}
                                        </el-checkbox>                  
                                    </el-checkbox-group>

                                </div>
                            </div>
                        </div>
                    </div>

                </div>

            </div>


            <div class="clearBox" v-show="isDeliverable">
                <h4 class="menu_h4">交付件属性配置</h4>
                <div class="half_row fl">
                    <label class="left fl required labelColor">交付件名称:</label>
                    <div class="right fl">
                        <input placeholder="必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; | \ /" 
                            type="text"  
                            maxlength="200" 
                            v-model.trim="deliverName" 
                            onfocus="this.placeholder=''" 
                            onblur="placeholder='必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; \| \\ \/'"
                            @blur="deliverBlur"
                            :class="{'error_border':isDeliver}" >
                        <div class="validBox" v-show="isDeliver">
                            <span class="line"></span>
                            <span class="fill"></span>
                        必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; | \ /</div>
                    </div>  
                </div>
                <div class="half_row fl">
                    <label class="left fl required labelColor">交付件类型:</label>
                    <div class="right fl">
                        <selected :list="deliverableTypeL"
                            :value.sync="deliverableType" 
                            :name="'attributes.ZH'"
                            :sign="'id'"
                            ref="deliverableType"
                            v-on:unfold="getDeliverableType" 
                            v-on:chose="choseDeliverableType"
                            :class="{'disable':!isDeliverableType,'error':isDeliverableTypeError}"
                            :placeholder="deliverableTypeName"></selected>
                        <div class="validBox" v-show="isDeliverableTypeError">
                            <span class="line"></span>
                            <span class="fill"></span>
                              请选择交付件类型
                        </div>
                    </div>  
                </div>
                <div class="half_row row_slt row_sltDoc fl">
                    <label class="left fl labelColor">产品:</label>
                    <div class="right fl">
                        <div class="doc_box">
                            <span class="inputbox">{{productName}}</span>
                            <span class="clear_btn" @click="clearPro" v-if="productName">X</span>
                            <span class="select_span select-btn" @click="selectProduct">选择</span>
                        </div>
                    </div>
                </div>
                <div class="half_row fl">
                    <label class="left fl labelColor">VR版本:</label>
                    <div class="right fl">
                        <selected :list="vrList" 
                                  :value.sync="vrId" 
                                  :name="'cn'"
                                  :sign="'id'"
                                  v-on:unfold="getVr" 
                                  v-on:chose="choseVr"
                                  :class="{'disable':!productId,'error':isVersion}"
                                  ref="vrVersion"
                                  :placeholder="vesVrName"
                                  ></selected>
                    </div>  
                </div>

                <div class="half_row fl">
                    <label class="left fl labelColor">C版本:</label>
                    <div class="right fl">
                        <selected :list="cList" 
                                  :value.sync="cId" 
                                  :name="'cn'"
                                  :sign="'id'"
                                  v-on:unfold="getC"
                                  v-on:chose="choseC"
                                  :class="{'disable':!vrId}"
                                  ref="cVersion"
                                  :placeholder="vesCName"
                                  ></selected>
                    </div>  
                </div>
                <div class="half_row fl" v-show="projectCategory != '1'">
                            <label class="left fl labelColor">浏览权限:</label>
                             <div class="right fl" v-if="false">
                                <div class="select_div">
                                <jobNumber ref="Permission"
                                    :class="{'errorBorder':isPermissions}"
                                    type="input"
                                    maxlength="50"
                                    :sign.sync="Permission"
                                    v-on:blurEvent="PermissionsBlur"
                                    ></jobNumber>
                                    <div class="validBox" v-show="isPermissions">
                                        <span class="line"></span>
                                        <span class="fill"></span>
                                    最多输入10个以;分隔</div>
                            </div>
                            </div>
                            <div class="right fl" >
                            <select   v-model = "Selected" style="width:100px; border:1px solid #ccc;"  @change="changePermissions">
                                <option v-for = "item in changeStatus" :value = "item.id">{{item.name}}</option>
                            </select>
                            <div class="select_div" v-show="false">
                                <jobNumber ref="Permission"
                                    :class="{'errorBorder':isPermissions}"
                                    type="input"
                                    maxlength="50"
                                    :sign.sync="Permission"
                                    v-on:blurEvent="PermissionsBlur"
                                    ></jobNumber>
                                    <div class="validBox" v-show="isPermissions">
                                        <span class="line"></span>
                                        <span class="fill"></span>
                                    最多输入10个以;分隔</div>
                            </div>
                            <!-- //<input   style="width:255px;margin-left:5px" :disabled="isDisabled"> -->
                            <div  class="sel_div Seledrop_info" v-show="!isDisabled">
                                <button class="sele toggle_info" expand="false" >{{Permission?Permission:'---请选择---'}}</button>
                                <ul class="sele-menu">
                           <!--          <li><el-checkbox v-model="checkAll" @change="selectAll">All</el-checkbox></li> -->
                                    <el-checkbox-group v-model="checkedArr" @change="oneChecked">
                                        <el-checkbox v-for="item in groupsArr"  :label="item.id" :key="item.id" :title="item.name" >{{item.name}}</el-checkbox>
                                    </el-checkbox-group>
                                </ul>
                            </div>
                            </div>
                        </div>


                <div class="half_row fl">
                    <label class="left fl labelColor">关键字:</label>
                    <div class="right fl">
                        <div class="right fl11111" style="width:100%;" @click.stop = "changeTolabel(deliverkeywords)">
                            <input type="text"  
                                   style="width:400px"
                                   maxlength="2000" 
                                   v-model.trim="deliverkeywords" 
                                   ref="autocomplete"
                                   placeholder="字符数少于20个,且不能包含以下字符 *  : ; ? , &quot; &lt; &gt; | \ /"
                                   v-if = "!this.showlabel"
                                   :trigger-on-focus="false"
                                   @keydown.enter = "keydownEvent($event,deliverkeywords)" 
                                   @blur="Blur($event)" 
                            />
                            <div  class="label"  v-show = "showlabel">
                                    <div class = "labelbox" 
                                        v-for="(item,index) in this.labels" 
                                        :key="index"   
                                        >
                                        <a ><span class="textlabelsapn" :title="item">{{item}}</span> <img src="../../assets/tab_close.png" @click.stop = "dellable(item,index,labels)"></a>
                                    </div>
                            </div>
                        </div>
                    </div>  
                </div>

                <div class="row fl">
                    <label class="left fl labelColor">摘要:</label>
                    <div class="right text_padding fl">
                        <textarea maxlength="2000" v-model="deliverDes"></textarea>
                    </div>
                </div>
            </div>



            <!-- 提交 -->
            <div class="btn_cont fl">
                <button class="btn btn-light" @click="submitTask()">确定</button>
                <button class="btn btn-normal" @click="submitTaskTemp">另存为任务模板</button>
                <button class="btn btn-normal" @click="cancletemp">取消</button>
            </div>
        </div>
        <!--  文档助手/模板文档弹窗 -->
        <docDialog v-on:getdoc = "getdoc" ref="doc"></docDialog>
        <!-- 相关资料弹窗 -->
        <relatedDocDialog ref="relateddoc" :relatedList="relatedList" @multiCaseChosen = 'addRelatdDoc'></relatedDocDialog>
        <!-- 产品 -->
        <productPbi v-model = "productScopeDialog" ref="product" @choseProduct = "choseProduct" :num = "num"></productPbi>
        <!--作者输入有误提醒-->
        <el-dialog :visible.sync="showOwnerErr" size="tiny" >
            <div style="padding: 0 10px;">以下人员信息有误，请更改。</div>
            <ul class="text_over" style="word-wrap: break-word;padding: 0 25px;">
                <li v-for="(item,index) in errOwners">{{item+";"}}</li>
            </ul>  
            <span slot="footer" class="dialog-footer btn_cont">
                <div class="btn_cont">
                    <button class="btn btn-light" @click="showOwnerErr=false">关闭</button>
                </div>
            </span>
        </el-dialog>
    </div>
</template>
<script>
    import '../../style/project.css'
    import docDialog from '../tasktemplate/docDialog.vue'
    import relatedDocDialog from '../tasktemplate/relatedDocDialog.vue'
    import productPbi from '../assemblies/productPBI.vue'
    import givenNumber from '../assemblies/givenNumber.vue'
    export default{
        components:{productPbi,docDialog, relatedDocDialog,givenNumber},
        data(){
            return {
                userId:D.sysUid,
                PermissionIds:'',
                groupsIds:'',
                checkedAll:false,
                checkedArr:[],
                groupsArr:[],
                //单个标签
                label:"",
                //标签数组
                labels:[],
                showlabel:true,
                titleName:'创建任务',
                isDeliverable: false,
                //名称
                title:"",
                isTitle:false,
                //责任人
                isOwner:false,
                owner:D.commonName,
                showOwnerErr:false,
                isPermissions:false,
                isTaskTemp:false,
                isDeliverableTypeError:false,
                Permission:'',
                errOwners:'',
                modelId:'',
                isDeliverableType:false,
                deliverableType:'',
                deliverableTypeL:[],
                deliverableTypeName:'---请选择交付件类型---',
                //任务说明
                taskDes:"",
                //计划完成时间
                planTime:'',
                isPlanTime:false,
                pickerLimit: {
                    disabledDate(time) {
                        return time.getTime() <  Date.now();
                    }
                }, 
                //任务类型
                taskTypeL:[],
                taskTypeId:"",
                taskTypeName:'---请选择任务类型---',
                isTaskType:false,
                //任务模板
                taskTempL:[],
                taskTempId:'',
                taskTempName:'---请选择任务模板---',
                //文档助手
                docAssistant:"",
                docName:"",
                //模板
                tempdocId:"",
                tempdocName:"",
                isTempdoc:false,
                //模板详情关键字
                searchWords:"",
                // type = 0  打开模板文档窗口，1为打开设计助手窗口
                type:'',
                //是否折叠模板选择 默认展开
                isfold:true,
                //辅助工具
                toolList:[],
                //已选的工具
                assisantTools:[],
                model : {},
                taskInfo : {
                    "id": "",
                    "taskName" : "",
                    "taskType" : "",
                    "status": "0",
                    "owner": "",
                },
                //产品选择
                productScopeDialog:false,
                num:1,
                productId:'',
                productName:"",
                //vr版本
                vesVrName:"---请选择VR版本---",
                vrList:[],
                vrId:'',
                //c版本
                cList:[],
                vesCName:"---请选择C版本---",
                cId:'',
                //交付件
                nodeId:'',
                deliverName:'',
                //交付件关键字
                deliverkeywords:'',
                //交付件摘要
                deliverDes:'',
                isDeliver:false,

                //相关资料(文档和参考说明的List),singlerelateDoc组成的数组，用于生产JSOn串
                relatedDoc:[],
                //相关资料的文档List,用于本页展示
                relateItems:[],
                //用于传递给子组件
                relatedList:[],
                //最大可选数量
                mostChose:100,
                // 编辑时用来缓存相关资料的数组
                relatedCopyList:[],

                //任务ID
                taskId:"",
                //交付表主键Id
                dleId:"",
                authorized:false,
                //项目id
                projectId:"",
                //项目名称
                projectName:"",
                Selected:4,
                isDisabled:true,
                changeStatus:[
                    {
                        "id":1,
                        "name":"内部公开"
                    },
                    {
                        "id":4,
                        "name":"项目公开"
                    },
                    {
                        "id":3,
                        "name":"指定组可见"
                    },
                    {
                        "id":2,
                        "name":"指定人可见"
                    },
                ],
                pid:'',
                supId:'',
                weight:'',
                iskeywords:false,
                isTaskdes:false,
                fileItems:[],
                //
                ischeck:true,
                templateId:'',
                 //故障工具新增领域
                domainList:[],
                domainId:"",
                allassisantTools:[],
                isIndeterminate:false,
                projectCategory:'', 
                project:{},
            }
        },
        created:function(){
            this.isAuthorized();
            //任务ID
            this.taskId = this.$route.query.taskId;
            if (this.taskId) {
                this.titleName ="编辑任务";
                this.getTaskDetail();
                return;
            }
            //项目ID
            this.projectId = this.$route.query.projectId;
            this.pid=this.projectId?this.projectId:'';
            //父任务ID
            this.supId = this.$route.query.suptaskId;
            //默认选中模板
            this.templateId=this.$route.query.templateId;
            //权重
            this.weight = this.$route.query.weight;
            //this.getTaskDetail();
            if (!this.templateId){
                this.getdomainbyAuxiliary([]);
            }
            this.getType(this.templateId);
            //this.getMyProject(this.projectId);
            this.getProjectInfo(this.projectId);
        },   
        mounted:function(){
            $(window).on('click',function(e){
                $('.Sel_permtype').removeClass('open');
                $('.toggle_permtype').attr('expand','false');

                $('.Seledrop_info').removeClass('open');
                $('.toggle_info').attr('expand','false');
            });


            $('#app').on('click','.toggle_permtype',function(e){
                var target = e.target;
                $('.Seledrop_info').removeClass('open');
                if( $('.Seledrop_info').find(".toggle_info")[0]){
                   $('.Seledrop_info').find(".toggle_info")[0].setAttribute('expand','false');
                }
                $('.Seledrop_info').removeClass('open');
                $('.toggle_info').attr('expand','false');


                $('.Sel_permtype').removeClass('open');
                $('.toggle_permtype').attr('expand','false');

                e.target.parentNode.classList.add('open');
                e.target.setAttribute('expand','true');
                e.stopPropagation();
            })

            $('#app').on('click','.Seledrop_info',function(e){
                if($('.Sel_permtype').find(".toggle_permtype")[0]){
                    $('.Sel_permtype').find(".toggle_permtype")[0].setAttribute('expand','false');
                }
                $('.Seledrop_info').removeClass('open');
                $('.toggle_info').attr('expand','false');

                $('.Sel_permtype').removeClass('open');
                $('.toggle_permtype').attr('expand','false');
                e.currentTarget.classList.add('open');
                e.currentTarget.firstChild.setAttribute('expand','true');
                e.stopPropagation();
            })

        },
        methods:{
            //获取个人项目id
            // getMyProject(id){
            //     let self = this;
            //     if(!id){
            //         id = "personal";
            //         this.projectId = "personal";
            //     }
            //     if(id == "personal"){
            //         this.$http.get("/project/taskextension/getMyProject.json",{
                        
            //         }).then(res=>{
            //             if(res && res.data){
            //                 this.projectId = res.data;
            //             }
            //         })
            //     }
            //     self.getGroups();
            // },

            //获取项目信息
            getProjectInfo(id){
                let self = this;
                if(!id || id == "personal"){
                    this.$http.get("/project/taskextension/getMyProject.json",{
                        
                    }).then(res=>{
                        if(res && res.data){
                            this.projectId = res.data;
                            self.projectCategory = '1';
                            self.getGroups();
                        }else{
                            D.showMsg(); 
                        }
                    })
                    
                }else{
                    this.$http({
                        method:'get',
                        url:'/project/project/getProjectBasicById.json',
                        params:{
                            projectId:id,
                            date : new Date().getTime(),
                        }
                    }).then(res => {
                        D.unblock();
                        if(res.data && res.data.head && res.data.head.flag){                       
                            if(res.data.body.projectBasicInfo){
                                self.project=res.data.body.projectBasicInfo;
                                self.projectCategory = res.data.body.projectBasicInfo.projectCategory;
                                self.getGroups();  
                            }
                        } else if(res.head.errorcode=='dfx.project.projectId.is.nullOrEmpty'){
                            D.showMsg("项目不存在或者已经被删除了");
                        }else {                      
                                D.showMsg();  
                        }

                    })
                }
                
            },

            changePermissions(){
                let  self  = this;
                if(this.Selected=='1' || this.Selected=='4'){
                    this.isDisabled=true;
                }else if(this.Selected=='2'){
                    this.isDisabled=false;
                    this.getGroupMembers(this.groupsIds);
                }
                else{
                    this.isDisabled=false;
                    this.getGroups();
                }
                this.Permission='';
                this.PermissionIds='';
                 setTimeout(function(){
                    self.$refs.Permission.text=this.Permission;
                },500);
            },
            //清空权限
            clearpermission(){
            let self = this;
            self.groupsArr = [];
            self.checkedArr = [];
            self.taskDialog = false;
        },
        //选中一个
        oneChecked(value) {
            this.Permission = value;
            this.getCheckedNames();
        },
        dltEmptyEl(arr){
            var res = [];
            for(var i=0; i<arr.length; i++){
                if(arr[i]){
                    res.push(arr[i]);
                }
            }
            return res;
        },
        //获取组
        getGroups(){
            let self = this;
            this.$http.get("/project/group/getProjectGroups.json",{
                params:{
                    projectId:this.projectId
                }
            }).then(res=>{
                res = res.data;
                if(res.body.message && res.body.message.length){
                    self.groupsArr = res.body.message;
                }else{
                    self.groupsArr = [];
                }
                // if(self.groupsArr.length <= 0){
                //     self.changeStatus = [
                //     {
                //         "id":1,
                //         "name":"内部公开"
                //     },
                //     {
                //         "id":4,
                //         "name":"项目公开"
                //     },
                   
                //     {
                //         "id":2,
                //         "name":"指定人可见"
                //     },];
                // }
                if(self.projectCategory == '0'){
                    if(self.groupsArr.length <= 0){
                            self.changeStatus = [
                        {
                            "id":1,
                            "name":"内部公开"
                        },
                        {
                            "id":4,
                            "name":"项目公开"
                        },
                       
                        {
                            "id":2,
                            "name":"指定人可见"
                        },];
                    }
                }else{
                    if(self.groupsArr.length <= 0){
                        self.changeStatus = [
                        {
                            "id":4,
                            "name":"项目公开"
                        },
                       
                        {
                            "id":2,
                            "name":"指定人可见"
                        },];
                    }else{
                         self.changeStatus=[
                        {
                            "id":4,
                            "name":"项目公开"
                        },
                        {
                            "id":3,
                            "name":"指定组可见"
                        },
                        {
                            "id":2,
                            "name":"指定人可见"
                        },
                    ]
                    }
                }
            })
        },
        //获人员
        getGroupMembers(groupsIds){
            let self = this;
            this.$http.post("/project/member/getProjectGroupMembers.json",{
                'projectId':this.projectId,
                'groups':groupsIds,
            }).then(res=>{
                res = res.data;
                if(res.body.message && res.body.message.length){
                    self.groupsArr = res.body.message;
                    self.getMemerCheckedNames();
                }else{
                    self.groupsArr = [];
                    //self.updatePerm(self.permType.value,'');
                }
            })
        },
        //获取选中的组
        getCheckedNames(){
            this.Permission ="";
            this.PermissionIds = "";
            for(var i =0;i< this.groupsArr.length;i++){
                for (var j=0;j< this.checkedArr.length;j++) {
                    if(this.groupsArr[i].id == this.checkedArr[j]){
                        this.Permission += this.groupsArr[i].name+';';
                        this.PermissionIds += this.groupsArr[i].id + ';';
                        console.log("this.Permission",this.Permission);
                        console.log("this.PermissionIds",this.PermissionIds);
                    }
                };
            }
            if(this.checkedArr.length == this.groupsArr.length){
                this.checkAll = true;
            }
        },
        //获取选中的成员
        getMemerCheckedNames(){
            this.Permission ="";
            this.PermissionIds = "";
            for(var i =0;i< this.groupsArr.length;i++){
                for (var j=0;j< this.checkedArr.length;j++) {
                    if(this.groupsArr[i].id == this.checkedArr[j]){
                        this.Permission += this.groupsArr[i].name+';';
                        this.PermissionIds += this.groupsArr[i].id + ';';
                        console.log("this.Permission",this.Permission);
                    }
                };
            }
            if(this.checkedArr.length == this.groupsArr.length){
                this.checkAll = true;
            }
        },

            getType(templateId){
                let self=this;
                if(!templateId){
                	 self.getTaskTypeL("PROJECT-customTask");   
                	return;
                }
                this.$http.get("/project/taskextension/getTaskTemplateTypeById.json?templateId="+templateId)
                .then(function(res){
                    res=res.data;
                    if(res && res.head && res.head.flag){
                        var type= res.body.message; 
                        if(type){
                             self.getTaskTypeL(type);   
                        }else{
                             self.getdomainbyAuxiliary([]);
                        }   
                    }else{
                        self.getdomainbyAuxiliary([]);
                        D.showMsg("查询模板信息失败，请联系管理员");
                    }
                }).catch(function(){
                    self.getdomainbyAuxiliary([]);
                    D.showMsg("查询模板信息失败，请联系管理员");
                }); 
            },
            //全部选中以及全部不选中切换
            checkTools(){
                let self = this;
                self.assisantTools=[];
                if(!self.ischeck){
                    self.ischeck=true;
                }else{
                    self.ischeck=false;
                    self.toolList.forEach(function(item){
                                self.assisantTools.push(item.id);
                            })
                }
                self.checkassistant();
            },
            // 删除标签
            dellable(lable,num,items){
                if(items && items.length > 0){
                    items.forEach(function(item,index){
                        if(index == num){
                            items.splice(num,1)
                        }
                    });
                    this.deliverkeywords = items.join(';');
                }
            },

            //点击标签转换为字符串
            changeTolabel(deliverkeyword){
                var self = this;
                this.showlabel = false;
                //自动聚焦
                this.$nextTick(() => {
                    this.$refs['autocomplete'].focus()
                });
                this.deliverkeywords = "";
                this.labels.forEach(item => {
                    this.deliverkeywords += item + ';';
                })
            },

            //清空标签
            Blur(event,deliverkeywords){
                let self = this;
                setTimeout(function(){
                    if(self.labels.length > 0 ){
                        self.showlabel = true;
                    }
                }, 100);
            },

            // 按回车键后
            keydownEvent(event,deliverkeywords){
                let self = this;
                var keyVal = event.keyCode;//键值
                // 回车键
                if(keyVal == 13){
                    var reg = /^[^*\/|:<>?,\\"]*$/;
                    let len = deliverkeywords.split(';').length;
                    self.label = deliverkeywords.split(';')[len - 1 ];
                    var curdeliverkeywords = this.labels.join(';');
                    if (!self.label) {
                        D.showMsg("请输入内容");
                        return;
                    };
                    if(!self.label || !reg.test(self.label)){
                        this.deliverkeywords = curdeliverkeywords;
                        D.showMsg("不能包含以下字符 *  : ; ? , &quot; &lt; &gt; | \ /");
                        return;
                    }
                    if(self.label.length > 20){
                        this.deliverkeywords = curdeliverkeywords;
                        D.showMsg("标签长度不能超过20");
                        return;
                    }
                    // 去重
                    if(self.label){
                        if(this.labels.length > 4){
                            this.deliverkeywords = curdeliverkeywords;
                            D.showMsg("标签最多为5个。");
                            return;
                        }
                        for(var i = 0; i < this.labels.length; i++){
                            if(self.label == this.labels[i]){
                                this.deliverkeywords = curdeliverkeywords;
                                D.showMsg("标签重复，请重新输入");
                                return;
                            }
                        } 
                        this.labels.push(self.label);
                        this.showlabel = true;
                    }
                }
            },
            submitTask(){
                let self=this;
                for (var i in self.allassisantTools){
                    var key =  Object.keys(self.allassisantTools[i])[0];
                    if(self.domainId==key)
                    {
                        self.allassisantTools[i][key]=self.assisantTools;
                    }
                }
                if(!self.projectId){
                    self.projectId='personal';
                }
                this.$http({
                    url:"/project/taskextension/submit/verify.json?"+(new Date()).getTime(),
                    method:"get",
                    params:{
                        projectId:self.projectId,
                        suptaskId:self.supId
                    }
                })
                .then(function(res){
                    res=res.data;
                    //创建任务时如果创建的是个人项目任务，不传项目ID
                    if(self.projectId=='personal'){
                        self.projectId='';
                    }
                    if(res && res.head && res.head.flag){
                         D.showMsg('确定提交吗？',function(){
                            self.doSubmitTask();
                        },true,true);
                    }else{
                        if(res.head.tips=='dfx.project.get.project.not.exist'){
                            D.showMsg("指定的项目不存在");
                        }else if(res.head.tips=='dfx.project.task.project.not.in.common'){
                            D.showMsg("任务对应的项目与要创建的项目不一致");
                        }else if(res.head.tips=='dfx.project.get.task.not.exist'){
                            D.showMsg("指定的任务不存在");
                            //指定父节点ID不存在，仍继续创建，清空supTaskId
                            //self.supId=''
                            //self.doSubmitTask();
                        }else if(res.head.tips=='dfx.project.no.task.add.power'){
                            D.showMsg("没有操作权限");
                        }else if(res.head.tips=='dfx.project.get.project.is.closed'){
                            D.showMsg("项目已关闭");
                        }else if(res.head.tips=='dfx.project.current.user.is.not.project.member'){
                            
                            // var msg="您不是"+self.project.name+"项目成员，需要加入到项目中吗？";
                            // D.showMsg(msg,function(){
                            //     self.doSubmitTask();
                            // },true,true);
                            //直接添加成员
                             D.showMsg('确定提交吗？',function(){
                                self.doSubmitTask();
                            },true,true);
                        }else{
                            D.showMsg("查询模板信息失败，请联系管理员");
                        }
                    }
                })
                .catch(function(){
                    D.showMsg("查询模板信息失败，请联系管理员");
                });             
                    
            },
            //提及任务
            doSubmitTask(){
                let self = this;
                this.titleBlur();
                if(this.isTitle){
                    return;
                }

                this.ownerBlur();
                if(this.isOwner){
                    this.isOwner = true;
                    return
                }
                this.PermissionsBlur();
                // if(!this.planTime){
                //     this.isPlanTime = true;
                //     return
                // }
                if(!this.taskTypeId){
                    this.isTaskType = true;
                    return
                } 
                //isDeliverable 为真为写作任务 模板文档不能为空
                if(this.isDeliverable){
                    /*if(!this.tempdocId){
                        this.isTempdoc = true;
                        return
                    }*/
                    this.deliverBlur();
                    if(this.isDeliver){
                        return
                    }
                    if (!this.deliverableType) {
                        this.isDeliverableTypeError = true; 
                        return;
                    }
                } 
                //组装relatedDoc数组
                this.relatedDoc = [];
                this.setRelateDoc();             
                var text=this.Permission;
                // if(this.Selected == '1'){
                //   this.power = 'ALL';
                // }else if(this.Selected == '4'){
                //   this.power = 'PDIS';
                // }else{
                //   this.power = this.PermissionIds;
                // }
                if(self.projectCategory == '2'){
                    if(this.Selected == '1'){
                      this.power = 'ALL';
                    }else if(this.Selected == '4'){
                      this.power = 'PDIS';
                    }else{
                        if(!this.PermissionIds.length){
                            this.power = 'PDIS';
                        }else{
                            this.power = this.PermissionIds;
                        }
                    }
                }else if(self.projectCategory == '0'){
                    if(this.Selected == '1'){
                      this.power = 'ALL';
                    }else if(this.Selected == '4'){
                      this.power = 'PDIS';
                    }else{                     
                      this.power = this.PermissionIds;
                    }
                }else{
                    // if(this.Selected == '4'){
                    //   this.power = 'PDIS';
                    // }else{                     
                    //     if(!this.PermissionIds.length){
                    //         this.power = 'PDIS';
                    //     }else{
                    //         this.power = this.PermissionIds;
                    //     }
                    // }
                    this.power = D.sysUid;
                }
                 //任务基本属性
                var taskInfo = {
                    weight:this.weight?this.weight:'',//权重
                    supTaskId:this.supId?this.supId:'',//父Id
                    projectId:this.projectId?this.projectId:'',//项目Id
                    taskId:this.taskId?this.taskId:'',     //任务ID
                    taskName:this.title,     //任务名称
                    owner:this.owner,     //责任人,多个以;分割开
                    status:this.status,     //任务状态  0:待分配 1:进行中  2:已完成 3:删除
                    taskType:this.taskTypeId,     //任务类型ID
                    taskTemplateId:this.taskTempId,     //任务模板id
                    description:this.taskDes,     //任务说明
                    docAssistant:this.docAssistant,     //文档助手        
                    templateDoc:this.tempdocId,     //模板文档NodeId
                    searchWords:this.searchWords,     //搜索关键字
                    relatedDocDesc:this.relatedDoc,     //相关资料及参考说明，以JSON格式存入 
                    auxiliaryTools:this.allassisantTools,
                         //辅助工具，元数据ID,分号隔开
                    finishTime:this.planTime,     //完成时间
                    //交付文档Id
                    nodeId:this.nodeId,

                    //交付件属性
                    deliverable:{
                        id:this.dleId,
                        deliverName:this.deliverName,
                        authorized:this.power,
                        product:this.productId,
                        deliverableType:this.deliverableType,
                        vrVersion:this.vrId,
                        cVersion:this.cId,
                        modelId:this.modelId,
                        keywords:this.deliverkeywords,
                        description:this.deliverDes,
                    },
                    fileItems:this.fileItems
                }
                console.log("this.nodeId", this.nodeId);    
                console.log("提交任务",taskInfo);
                self.veriOwner(taskInfo,text);
               
            },
            //责任人校验
            veriOwner(taskInfo){
                let self = this;
                var userIds = taskInfo.owner;
                //验证人员信息
                D.veriUser({
                    userIds:D.jobNumberStrSpace(userIds),
                    success(){
                        self.taskQuery(taskInfo);
                    },
                    error(arr){
                        D.unblock();
                        if(arr){
                            self.showOwnerErr = true;
                            self.errOwners = arr;
                        }else{
                            D.showMsg();
                        }
                    }
                })
            },
            //任务请求
            taskQuery(taskInfo){
                let self = this;
                let url = "",tipMng="";
                if(taskInfo.taskId){
                    url= '/project/taskextension/update.json';
                    tipMng ="修改成功！"
                }else{
                    url = '/project/taskextension/create.json';
                    tipMng ="创建成功！"
                }
                D.block();
                this.$http.post(url, taskInfo, {
                    contentType:'application/json',
                })
                .then(function(res){
                    D.unblock();
                    res=res.data;
                    if(res.head.flag){
                        setTimeout(function(){
                            //project/detail/PRO1000001966?taskId=TAS1000016150
                            window.location.href='/project/detail/' + res.head.tips+"?taskId="+res.body.message;
                        },100)  
                    }else{
                        //错误码
                        if(res.head.tips == "dfx.projectwebsite.task.exist.task.name"){
                            D.showMsg("任务重名,请修改！");
                        }else if (res.head.tips == "dfx.projectwebsite.task.exist.dle.name") {
                            D.showMsg("交付件重名,请修改！");
                        }else if (res.head.tips == "dfx.projectwebsite.create.task.failed") {
                            D.showMsg("创建任务失败！");
                        }else if (res.head.tips == "dfx.projectwebsite.update.task.failed") {
                            D.showMsg("更新任务失败！");
                        // }else if (res.head.tips == "dfx.projectwebsite.dle.not.exist") {
                        //     D.showMsg("交付件不存在,请联系管理员！");
                        // }else if (res.head.tips == "dfx.projectwebsite.add.personal.project.failed") {
                        //     D.showMsg("添加个人项目失败！");
                        // }else if (res.head.tips == "dfx.projectwebsite.add.project.task.failed") {
                        //     D.showMsg("添加项目和任务关系失败！");
                        } else {
                            D.showMsg("提交失败!");
                        }
                    }
                 
                })
                .catch(function(){
                    D.unblock();
                    D.showMsg('创建失败,网络服务异常,请联系管理员!');
                });
            },
            //删除相关资料
            delRelatedDoc(item){
                console.log("item",item);
                var index = this.relateItems.indexOf(item);
                this.relateItems.splice(index,1);
                // console.log("this.caseItems",this.caseItems);
                this.mostChose = 100 - this.relateItems.length;
            },
            addRelatdDoc(list){
                //let _this = this;
                console.log('list:',list);
                console.log('this.relatedCopyList:',this.relatedCopyList);
                this.relateItems = list;
                if (this.relatedCopyList.length > 0) {
                    for (var i = 0; i < this.relateItems.length; i++) {
                        for (var j = 0; j < this.relatedCopyList.length; j++) {
                            if (this.relateItems[i].id == this.relatedCopyList[j].id) {
                                this.relateItems[i].des = this.relatedCopyList[j].des;
                            };
                        };
                    };
                };
                for (var i = 0; i < this.relateItems.length; i++) {
                    for (var j = 0; j < this.relatedCopyList.length; j++) {
                        if (this.relateItems[i].id == this.relatedCopyList[j].id) {
                            this.relateItems[i].des = this.relatedCopyList[j].des;
                        };
                    };
                };
                this.mostChose = 100 - list.length;
                console.log("this.relateItems",this.relateItems);
                if (this.mostChose < 0) {
                    D.showMsg("最多可选择100个相关资料");
                };
                this.$refs.relateddoc.relatedDocDialog = false;
                this.$refs.relateddoc.relatedList = [];
            },
            //选择相关资料
            showRelatedDocDialog(){
                let self = this;
                self.relatedList = self.relateItems;
                this.$refs.relateddoc.relatedDocDialog = true;
                setTimeout(function(){
                    self.$refs.relateddoc.setRelatedList();
                },500);
            },
            //组装relatedDoc数组
            setRelateDoc(){
                let self = this;
                let ris = this.relateItems;
                if (ris && ris.length > 0) {
                    // for (let i = ris.length - 1; i >= 0; i--) {
                    for (let i = 0; i <= ris.length - 1; i++) {
                        var sdoc ={
                            nodeId:"",
                            referenceDesc:"",
                        }
                        sdoc.nodeId = ris[i].id;
                        sdoc.referenceDesc = ris[i].des;
                        self.relatedDoc.push(sdoc);
                    };
                };
            },
            //提交任务模板
            submitTaskTemp(){
                let self = this;
                this.titleBlur();
                for (var i in self.allassisantTools){
                    var key =  Object.keys(self.allassisantTools[i])[0];
                    if(self.domainId==key)
                    {
                        self.allassisantTools[i][key]=self.assisantTools;
                    }
                }
                if(this.isTitle){
                    return;
                }
                if(!this.taskTypeId){
                    this.isTaskType = true;
                    return
                }
                //组装relatedDoc数组
                this.relatedDoc = [];
                this.setRelateDoc();
                var taskTemplateEx ={
                    ttId:'',
                    taskTempName:this.title,
                    taskType:this.taskTypeId,
                    taskDescription:this.taskDes,
                    templateDoc:this.tempdocId,
                    docAssistant:this.docAssistant,                  
                    searchKeyword:this.searchWords,
                    relatedDocDesc:this.relatedDoc,
                    auxiliaryTools:this.allassisantTools,
                }
                console.log("另存为模板obj",taskTemplateEx)
                D.showMsg('确定创建任务模板吗？',function(){
                    self.taskTempQuery(taskTemplateEx)
                },true,true);

            },
            //提交任务模板请求
            taskTempQuery(taskTemplateEx){
                
                let self = this;
                let url = '/project/tasktemplate/create.json';
                D.block()  
                this.$http.post(url,taskTemplateEx,{
                    contentType:'application/json',
                })
                .then(function(res){
                    D.unblock();
                    res=res.data;
                    if(res.head.flag){
                        //D.showMsg('另存为任务模板成功');
                         
                    }else{
                        if(res.head.errorcode=='tasktempname.exist'){
                            D.showMsg("已经存在相同的任务模板名称");
                        }else{
                            D.showMsg('另存为任务模板失败，请联系管理员！');
                        }
                    }
                 
                })
                .catch(function(){
                    D.unblock();
                    D.showMsg('另存为任务模板失败,网络服务异常,请联系管理员!');
                });
            },
            //产品选择
            choseProduct(value){
                this.productName = value.text;
                this.productId = value.productIds[0];          
                this.restversion();
            },
            //重置所有版本信息
            restversion(){
                this.cList=[];
                this.vrList=[];
                this.vrId="";
                this.cId="";
                this.vesVrName = "---请选择VR版本---";
                this.vesCName = "---请选择C版本---";
                this.$refs.vrVersion.placeholder = "---请选择VR版本---";
                this.$refs.cVersion.placeholder ="---请选择C版本---";
                this.$refs.vrVersion.reset();
                this.$refs.cVersion.reset();
            },
            //获取vr版本
            getVr(){
                const self = this;
                D.block();
                this.$http.get("/project/pbi/org/edition/vr/offering.json",{
                    params:{"sourceId":self.productId}})
                .then(function(res){
                    D.unblock();
                    res = res.data;
                    if(res.body.pbiNodes){
                        self.vrList = res.body.pbiNodes;
                    }else{
                        self.vrList = [];
                    }
                })
                .catch(function(){
                    D.unblock();
                    D.showMsg();
                });
            },
            //获取c版本
            getC(){
                const self = this;
                D.block();
                this.$http.get("/project/pbi/org/edition/c/vr.json",{
                    params:{"sourceId":self.vrId}})
                .then(function(res){
                    D.unblock();
                    res = res.data;
                    if(res.body.pbiNodes){
                        self.cList = res.body.pbiNodes;
                    }else{
                        self.cList = [];
                    }
                })
                .catch(function(){
                    D.unblock();
                    D.showMsg();
                });
            }, 
            //选择VR版本
            choseVr(item){
                this.cList=[];
                this.cId ="";
                this.ves_W="";       
            },
            choseC(item){
            },
            //选择辅助工具
            checkinlist(toolId){
                this.checkassistant();
                console.log("辅助工",this.assisantTools)
          /*      console.log(toolId)
                console.log(this.tools)*/
            },
            //获取任务类型
            getTaskTypeL(templateTpye){
                let self = this;
                if (self.taskTypeL &&　self.taskTypeL.length > 0) {
                    return;
                }
                D.block();
                self.$http({
                    url:'/project/meta/getSubterms.json?'+(new Date()).getTime(),
                    method:"get",
                    params:{
                      tid:"PROJECT-TASKTYPE"
                    }
                }).then(res => {
                    D.unblock();
                    console.log("res",res.data)
                    res =res.data;
                    if(res.head.flag){
                        self.taskTypeL = res.body.message;
                        if(templateTpye){                           
                            self.defaultSelTaskType(templateTpye);
                            self.getTaskTempL();
                        }
                    }else{
                        D.showMsg("获取任务类型失败，请联系管理员！")
                    }
                });
            },
            defaultSelTaskType(templateTpye) {
                let self=this;
                self.taskTypeL.forEach(function(item){
                    if(item.id==templateTpye){
                        self.taskTypeName=item.attributes.ZH;
                        //self.$refs.taskType.choseName=self.taskTypeName;
                        self.choseTaskType(item);
                        self.taskTypeId=templateTpye;
                    }
                });
            },
            //选中任务类型
            choseTaskType(type) {
                let self = this;
                this.isTaskType = false;
                console.log("type", type);
                //初始化任务模板详情
                this.initTaskTempInfo(type);
                
                //初始化交付件属性
                this.initDeliverableConfig(type);
            },
            //初始化是否全部选中取消
            checkassistant(){
                let  self = this;
                if(self.assisantTools.length>0 && self.assisantTools.length==self.toolList.length){
                    self.ischeck=true;
                    self.isIndeterminate=false;
                }
                else if(self.assisantTools.length==0){
                    self.ischeck=false;
                    self.isIndeterminate=false;
                }else{
                    self.isIndeterminate=true;
                }
            },
            getDeliverableType() {

            },
            //选中交付件类型
            choseDeliverableType(type) {
                this.isDeliverableTypeError = false;
            },
            initTaskTempInfo(type) {
                //清空模板详情
                //文档助手
                this.docAssistant = '';
                this.docName =  '';
                //模板
                this.tempdocId = '';
                this.tempdocName = '';
                this.isTempdoc = false;
                //搜索关键字
                this.searchWords = '';
                //相关资料
                this.relateItems = [];
                this.relatedList = [];
                this.relatedCopyList = [];
                //已选的辅助工具
                this.assisantTools = [];
                //显示交付件配置信息
                if (type &&　type.attributes && type.attributes.isTaskTempInfo == 'true') {
                    this.isTaskTemp = true;
                } else {
                    this.isTaskTemp = false;
                }
                
            },
            //初始化交付件配置信息
            initDeliverableConfig(type) {
                //清空任务模板
                this.taskTempL =[];
                this.taskTempId ='';
                this.taskTempName = '---请选择任务模板---';
                this.$refs.taskTemp.placeholder = this.taskTempName;
                this.$refs.taskTemp.reset();
                
                //初始交付文档属性
                //交付件
                this.deliverName = '';
                //交付件关键字
                this.deliverkeywords = '';
                //交付件摘要
                this.deliverDes = '';
                this.isDeliver =false;
                
                //初始化交付件类型
                this.deliverableType = '';
                this.deliverableTypeL = [];
                this.deliverableTypeName = '---请选交付件类型---';
                this.$refs.deliverableType.placeholder = this.deliverableTypeName;
                this.$refs.deliverableType.reset();
                
                //显示交付件配置信息
                if (type &&　type.attributes && type.attributes.isDeliverable == 'true') {
                    this.isDeliverable = true;
                    this.isDeliverableType = true;
                    this.modelId = type.attributes.mid;
                    this.initDeliverable(type.attributes.deliverableType);
                    return;
                    //动态初始化交付件属性。代码先写死，后续需要在放开，下面代码不执行
                    this.initDeliverableAttribute(this.modelId);
                } else {
                    this.isDeliverable = false;
                }
            },
            //初始化交付件类型下拉框
            initDeliverable(deliverableType) {
                let self = this;
                if (deliverableType) {
                    D.block();
                    self.$http({
                        url:'/project/meta/getSubterms.json?'+(new Date()).getTime(),
                        method:"get",
                        params:{
                            tid: deliverableType
                        }
                    }).then(res => {
                        D.unblock();
                        console.log("res",res.data)
                        res =res.data;
                        if(res.head.flag){
                            self.deliverableTypeL = res.body.message;
                        }else{
                            D.showMsg("获取交付件类型失败，请联系管理员！")
                        }
                    });
                }
            },
            initDeliverableAttribute(mid) {
                self.$http({
                    url:'/project/model/getModelByMid.json?'+(new Date()).getTime(),
                    method:"get",
                    params:{
                      mid: mid
                    }
                }).then(res => {
                    console.log("model",res.data)
                    res =res.data;
                    if(res.head.flag){
                        self.model = res.body.message;
                    }else{
                        D.showMsg("获取任务类型失败，请联系管理员！")
                    }
                });
            },
             //获取辅助工具所属领域
            getdomainbyAuxiliary(tools){
                let self = this;
                self.allassisantTools=[];
                self.$http({
                    url:'/project/meta/topterms.json?'+(new Date()).getTime(),
                    method:"get",
                    params:{
                      vid:"AuxiliaryTools"
                    }
                }).then(res => {
                    console.log("res",res.data)
                    res =res.data;
                    if(res.head.flag){
                        self.domainList = res.body.message;
                        if(self.domainList.length>0)
                        {
                            for(var i in self.domainList)
                            {
                                var title = self.domainList[i].id;
                                var name = {};
                                name[title] =[];
                                self.allassisantTools.push(name);
                                if(self.domainList[i].attributes && self.domainList[i].attributes.ZH){
                                    self.domainList[i].attributes.ZH.split("_").join("/");
                                }
                            }
                            if(tools && tools.length>0){
                                for(var k in tools)
                                {
                                    for (var y in self.allassisantTools)
                                    {
                                        var key  = Object.keys(self.allassisantTools[y])[0];
                                        var tool = Object.keys(tools[k])[0];
                                        if (key==tool)
                                        {
                                            self.allassisantTools[y][key] = tools[k][tool];
                                        }
                                    }
                                }

                            }
                            
                            self.getAssistant(self.domainList[0].id);
                        }
                       /* if(!self.assisantTools.lenght){
                            self.toolList.forEach(function(item){
                                self.assisantTools.push(item.id);
                            })
                        }*/

                    }else{
                        D.showMsg("获取辅助工具失败，请联系管理员！")
                    }

                });
            },
            getAssistant(id){
                let self = this;
                self.domainId=id;
                self.getAidedToolsByTid(id);
            },
            getTools(id){
                let self = this;
                for (var i in self.allassisantTools){
                    var key =  Object.keys(self.allassisantTools[i])[0];
                    if(self.domainId==key)
                    {
                        self.allassisantTools[i][key]=self.assisantTools;
                    }
                }
                self.domainId=id;
                self.getAidedToolsByTid(id);
            },
            //根据tid获取领域工具
            getAidedToolsByTid(id){
                let self = this;
                //self.assisantTools=[];
                //self.toolList=[];
                self.$http({
                    url:'/project/meta/getSubterms.json?'+(new Date()).getTime(),
                    method:"get",
                    params:{
                      tid:id
                    }
                }).then(res => {
                    console.log("res",res.data)
                    res =res.data;
                    if(res.head.flag){
                        self.toolList = res.body.message;
                        var index=0;
                        if(self.allassisantTools.length>0)
                        {
                            var key =  Object.keys(self.allassisantTools)
                            for (var j in key){
                                if (key[j]==id){
                                    index =j;
                                }
                            }
                            for (var i in self.allassisantTools)
                            {
                                var key =  Object.keys(self.allassisantTools[i])[0];
                                if(key==id){
                                    self.assisantTools = self.allassisantTools[i][key];
                                }
                                
                            }
                            self.checkassistant();
                        }
                        
                       /* if(!self.assisantTools.lenght){
                            self.toolList.forEach(function(item){
                                self.assisantTools.push(item.id);
                            })
                        }*/

                    }else{
                        D.showMsg("获取辅助工具失败，请联系管理员！")
                    }

                });
            },
            //获取任务模板
            getTaskTempL(){
                let self = this;
                if (!self.taskTypeId) {
                    return;
                }
                D.block();
                self.$http({
                    url:'/project/tasktemplate/getTaskTemplateByType.json?'+(new Date()).getTime(),
                    method:"get",
                    params:{
                      type: self.taskTypeId
                    }
                }).then(res => {
                    D.unblock();
                    console.log("taskTempL",res.data)
                    res = res.data;
                    if(res.head.flag){
                        self.taskTempL = res.body.message;
                        self.defaultTempType();
                    }else{
                        D.showMsg("获取任务模板信息失败，请联系管理员！")
                    }
                }); 
            },
            defaultTempType(){
                if(!this.templateId){
                    return;
                }
                let self=this;
                self.taskTempL.forEach(function(item){
                    if(item.ttId==self.templateId){
                        self.taskTempName=item.taskTempName;
                        self.$refs.taskTemp.choseName=self.taskTempName;
                        self.taskTempId=self.templateId;
                        self.choseTaskTemp();
                    }
                });
            },
            appendUrl(temlpateId){
                var param=this.$route.query;
                param.templateId=temlpateId;
                var newUrl ="";
                for(var key in param){
                    if(newUrl==""){
                        newUrl="?"+key+"="+param[key];
                    }else{
                         newUrl+="&"+key+"="+param[key];
                    }
                }
                history.pushState(param,null,newUrl)
            },
            //选择任务模板
            choseTaskTemp(){
                let self = this;
                let taskTempEx = "";
                if(!this.taskTempId){
                    return;
                }
                self.appendUrl(this.taskTempId);
                this.$http.get("/project/tasktemplate/getTaskTemplateDetail.json?"+(new Date()).getTime(),{
                    params:{'tasktempId':self.taskTempId}})
                .then(function(res){
                    res = res.data;
                    console.log("模板详情",res)
                    if(res.head.flag){
                        taskTempEx = res.body.taskTemplateEx;
                        //任务类型
                        self.taskTypeId = taskTempEx.taskType;
                        self.taskTypeName = taskTempEx.templateTypeName;
                        //文档助手
                        self.docAssistant = taskTempEx.docAssistant;
                        self.docName =  taskTempEx.docAssistantName;
                        //模板文档
                        self.tempdocId = taskTempEx.templateDoc;
                        self.tempdocName = taskTempEx.templateDocName;
                        //清空选择本地文档
                        self.fileItems = [];    
                        //关键字
                        self.searchWords = taskTempEx.searchKeyword;
                        //相关资料
                        self.relateItems = taskTempEx.relateItems  || [];
                        self.relatedCopyList = taskTempEx.relateItems  || [];
                        //已选的辅助工具
                        //self.assisantTools = taskTempEx.assisantToolsList || [];
                        self.getdomainbyAuxiliary(taskTempEx.auxiliaryTools);
                    }else{
                        D.showMsg("获取任务模板失败，请联系管理员！")
                    }
                })
                /*.catch(function(){
                    D.showMsg();
                });*/
            },
            //选择模板文档、文档助手
            showDocDialog(type){
                let self = this;
                this.type = type
                this.$refs.doc.docDialog = true;
                if(this.type =="0"){
                    this.$refs.doc.id = this.tempdocId;
                    this.$refs.doc.dialogName = '模板文档';
                }else{
                    this.$refs.doc.id = this.docAssistant; 
                    this.$refs.doc.dialogName = '设计向导'; 
                }
                this.$refs.doc.dialogType = this.type;
                this.$refs.doc.searchreset();
            },
            //回显模板文档、文档助手
            getdoc(data){
                if(this.type =="0"){
                    this.tempdocName = data.name;
                    this.tempdocId = data.id;
                    this.fileItems = [];
                    if(this.tempdocId){
                      this.isTempdoc = false;      
                    }  
                }else{
                    this.docName = data.name;
                    this.docAssistant = data.id;  
                }

            },
            selectProduct() {
                this.$refs.product.topTermQuery();                  
                this.productScopeDialog = true;
            },
            //清除模板文档
            cleartempdoc(){
                this.tempdocName = "";
                this.tempdocId = "";  
                this.fileItems = [];
            },
            //清除文档助手
            cleardoc(){
                this.docName = "";
                this.docAssistant = "";
            },
            //清除产品
            clearPro(){
                this.productId = '';
                this.productName = "";
                this.restversion();
            },
            //计划完成时间
            changePick(){
                this.isPlanTime = false;
            },
            //标题校验
            titleBlur(){
                var reg = /^[^*\/|:<>?\\"]*$/;
                if(!this.title || !reg.test(this.title)){
                    this.isTitle = true;
                }else{
                    this.isTitle = false;
                }
            },
            //交付件标题校验
            deliverBlur(){
                var reg = /^[^*\/|:<>?\\"]*$/;
                if(!this.deliverName || !reg.test(this.deliverName)){
                    this.isDeliver = true;
                }else{
                    this.isDeliver = false;
                }
            },
            //责任人校验
            ownerBlur(str=this.owner){
                var len = D.jobNumberArr(str).length;
                if(len>20){
                    this.isOwner = true;
                }else{
                    this.isOwner = false;
                }
            },
            //浏览权限校验
            PermissionsBlur(str=this.Permission){
                var len = D.jobNumberArr(str).length;
                if(len>10){
                    this.isPermissions = true;
                }else{
                    this.isPermissions = false;
                }
                
            },
            getTaskDetail(){
                let self=this;
                if(!this.taskId){
                    self.getdomainbyAuxiliary([]);
                    return;
                }
                this.$http.get("/project/task/getTaskDetail.json?"+(new Date()).getTime(),{
                    params:{'taskId':self.taskId}})
                .then(function(res){
                    res=res.data;
                    if(res.head.flag){
                        console.log("username:"+D.userName);
                        var node=res.body.deliverableNode;
                        var taskInfo=res.body.taskInfoEx;
                        //非责任者，后台管理员没有权限
                        if(!taskInfo.ownerList.includes(D.userName) && !self.authorized){
                            self.$router.replace('/nopermission');
                            return;
                        }
                        if(taskInfo){
                            self.title=taskInfo.taskName;
                            if(taskInfo.ownerList && taskInfo.ownerList.length>0){
                                self.owner = taskInfo.ownerList.join(";")+";";
                                self.$refs.author.setText(self.owner);
                            }
                            self.status=taskInfo.status;
                            //任务类型
                            self.taskTypeId=taskInfo.taskType;
                            self.taskTypeName = taskInfo.taskTypeName;
                            self.taskTempId=taskInfo.taskTemplateId;
                            self.taskTempName=taskInfo.taskTemplateName;
                            self.taskDes=taskInfo.description;
                            //文档助手
                            self.docAssistant=taskInfo.docAssistant;
                            self.docName =  taskInfo.docAssistantName;
                            //模板文档
                            self.tempdocId=taskInfo.templateDoc;
                            self.tempdocName=taskInfo.templateDocName;
                            //关键字
                            self.searchWords=taskInfo.searchWords;
                            //相关资料
                            self.relateItems=taskInfo.relateItems || [];
                            //缓存原有的参考说明
                            self.relatedCopyList = [];
                            self.relatedCopyList = taskInfo.relateItems  || [];
                            self.planTime=taskInfo.finishTime;
                            //交付文档Id
                            self.nodeId = taskInfo.nodeId;
                            //交付件主键Id
                            self.dleId=res.body.dleId;
                            //已选的辅助工具
                            //self.assisantTools = taskInfo.assisantToolsList || [];
                            self.getdomainbyAuxiliary(taskInfo.auxiliaryTools);
                            console.log("this.nodeId", self.nodeId)   
                        }
                        //交付件属性
                        if(node){
                            self.isDeliverable = true;
                            self.deliverName=node.name;
                            var field=node.fieldValues;
                            self.productName=res.body.scopeName;
                            if(field.product){
                                var productIds = field.product.values;
                                if(productIds.length==1){
                                    self.productId = productIds[0];
                                }
                            }
                            if(field.vrVersion &&field.vrVersion.values.length>0){
                                self.vrId = field.vrVersion.values[0];
                                self.vesVrName=res.body.vrverionName;
                            }
                            if(field.cVersion && field.cVersion.values.length>0){
                                self.cId = field.cVersion.values[0];
                                self.vesCName=res.body.cverionName;
                            }    
                            self.deliverkeywords=node.keywords;
                            self.labels = [];
                            var nodekeywords = [];
                            if (node.keywords) {
                                nodekeywords = node.keywords.split(";");
                                var len = nodekeywords.length;
                                var num = len - 1;
                                nodekeywords.forEach(item=>{
                                    if(item == ''){
                                        nodekeywords.splice(num,1);
                                    }else{
                                        return;
                                    } 
                                })    
                                self.labels = nodekeywords;
                            }
                            self.deliverDes=node.description;
                        }
                    }else{
                        self.getdomainbyAuxiliary([]);
                        D.showMsg("获取任务详情失败，请联系管理员");
                    }
                    console.log("task:"+res.data);
                });
            },
            //是否后台管理员
            isAuthorized(){
                let self=this;
                if(D.allRoles){
                    D.allRoles.forEach(function(item){
                        if('DFXRoleType-BEAdmin'==item.roleType){
                            self.authorized=true;
                            return;
                        }
                    });
                }
            },
            cancletemp(){
                let self = this;
                self.checkedArr = [];
                self.groupsArr = [];
                window.location.href="/myspace/project/list";
            },
            keywordsBlur(){
                this.searchWords=this.searchWords.trim();
                if(this.searchWords &&this.searchWords.length>100){
                    this.iskeywords=true;
                }else{
                    this.iskeywords=false;
                }
            }, 
            taskdesBlur(){
                if(this.taskDes.length>1000){
                    this.isTaskdes=true;
                }else{
                    this.isTaskdes=false;
                }
            },
            viewDoc(nodeId){
                if(!nodeId){
                    return;
                }
                D.block();
                this.$http.get("/project/document/checkHaveIdp.json",{
                    params:{"nid":nodeId}})
                .then(function(res){
                    D.unblock();
                    res = res.data;
                    if(res.head.flag){
                        if(res.body.flag){
                            window.open("/document/publish/idpview/"+nodeId);
                        }else{
                            window.open("/document/detail/"+nodeId);
                        }
                    }else{
                        D.showMsg("检测idp文档失败，请联系管理员");
                    }
                })
                .catch(function(){
                    D.unblock();
                    D.showMsg();
                });
            },
            fileChange(inputEle){

                let self = this;
                let file = this.$refs.inputer.files;
                //解决IE11上传附件触发filechange事件两次的问题
                // if(file.length == 0 && self.contentInfoList.length == self.toDeletePartNoList.length){
                if(file.length == 0){
                    return;
                }
                //判断文件个数
                let formdata = new FormData();
                //文件插入到formData
                for(var i = 0;i < file.length;i++){
                    // 附件名称长度判断，不能超过80个字符
                    var name = file[i].name;
                    file[i].firstName = file[i].name.split('.')[0];
                    if(file[i].size < 1){
                        D.showMsg("选择的文件内容不能为空，请重新选择");
                        return this.$refs.inputer.value='';
                    }
                    if(file[i].size/1024/1024 > 500){
                        D.showMsg("选择的文件不能大于500M，请重新选择");
                        return this.$refs.inputer.value='';
                    }
                    if(!name.endsWith(".doc") && !name.endsWith(".docx")){
                        D.showMsg("选择的文件只能选择Word文档，请重新选择");
                        return this.$refs.inputer.value='';
                    }
     
                    formdata.append('file',file[i]);
                }; 
                this.$refs.inputer.value = '';
                //发送formdata到临时目录，返回一个数组
                D.block();
                this.$http.post('/project/upload/tempAttachments.json',formdata,{
                    headers:{
                        "Content-Type": "multipart/form-data"
                    }
                }).then(res => {
                    D.unblock();
                    res = res.data;
                    if(res.head && res.head.flag){
                        self.fileItems = res.body.message;
                        if(self.fileItems){
                            var fileName = self.fileItems[0].FILE_NAME;
                            //模版文档
                            self.tempdocName = fileName;
                            //交付件名称
                            self.deliverName = fileName.substring(0,fileName.lastIndexOf("."));
                            self.tempdocId = "";
                        }
                    } else {
                         D.showMsg("添加文件失败");
                    }
                }).catch(res => {
                  D.showMsg("服务器错误，请重新上传.")})
            },
        },
        props:['indexItem','pId']
    }
</script>
<!--taskEnd-->
<!--taskDialogStart-->
<template>
       <div class="addTask" v-if="taskDialog">
        <el-dialog :title ="dialogName" size = "taskSize" class = "taskDialog" custom-class = "el-dialog--taskSize" :before-close="clearpermission" :visible.sync = "taskDialog" :close-on-click-modal = "false" style="margin-top: -10vh;">
            <div class="addtask project clearBox">
        <!--基础信息-->
                <div class="taskCont clearBox">
                    <h4 class="menu_h4">基本信息配置</h4>
                    <div class="half_row fl">
                        <label class="left fl required labelColor">任务名称:</label>
                        <div class="right fl">
                            <input placeholder="必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; | \ /"
                                type="text"
                                maxlength="200"
                                ref="taskName"
                                v-model.trim="title"
                                onfocus="this.placeholder=''"
                                autofocus="autofocus"
                                onblur="placeholder='必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; \| \\ \/'"
                                @blur="titleBlur"
                                :class="{'error_border':isTitle}" >
                            <div class="validBox" v-show="isTitle">
                                <span class="line"></span>
                                <span class="fill"></span>
                            必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; | \ /</div>
                        </div>
                    </div>


                    <div class="half_row fl">
                        <label class="left fl labelColor">计划完成时间:</label>
                        <div class="right fl">
                            <div class="block" >
                                <el-date-picker
                                  v-model="planTime"
                                  type="date"
                                  :placeholder="planTimeName"
                                  format="yyyy-MM-dd"
                                  value-format="yyyy-MM-dd"
                                  :picker-options="pickerLimit"
                                  :editable="false"
                                  :class="{'errorBorder':isPlanTime}"
                                  @change="changePick">
                                </el-date-picker>
                            </div>
                            <div class="validBox" v-show="isPlanTime">
                                <span class="line"></span>
                                <span class="fill"></span>
                            必填，请选择计划完成时间</div>
                        </div>
                    </div>

                    <div class="row fl">
                        <label class="left fl labelColor">责任人:</label>
                        <div class="right fl" v-show="this.projectCategory == '1'">
                            <div class="select_div" style="width:100%;">
                                <input 
                                        disabled="disabled"
                                        style="width:92.5%"
                                        type="text"
                                        maxlength="200"
                                        ref="author"
                                        v-model.trim="owner" >
                            </div>
                        </div>
                        <div class="right fl" v-show="this.projectCategory != '1'">
                            <div class="select_div" style="width:100%;">
                                <jobNumber ref="author"
                                    :class="{'errorBorder':isOwner}"
                                    style="width:92.5%"
                                    type="input"
                                    maxlength="10000"
                                    :sign.sync="owner"
                                    v-on:blurEvent="ownerBlur"
                                    ></jobNumber>
                                    <div class="validBox" v-show="isOwner">
                                        <span class="line"></span>
                                        <span class="fill"></span>
                                    责任人最多输入20个以;分隔</div>
                            </div>
                        </div>
                    </div>

                    <div class="row fl">
                        <label class="left fl labelColor">任务说明:</label>
                        <div class="right fl">
                        <textarea cols="30" rows="3" style="overflow-y:hidden;width:92.5%"
                            v-model="taskDes" maxlength="1000"
                            @blur="taskdesBlur"
                                :class="{'error_border':isTaskdes}"
                         ></textarea>
                            <div class="validBox" v-show="isTaskdes">
                                <span class="line"></span>
                                <span class="fill"></span>
                            任务说明不能超过1000字符</div>
                        </div>
                    </div>
                    <h4 class="menu_h4 fl">任务模板选择</h4>
                    <div class="half_row fl">
                        <label class="left required fl labelColor">任务类型:</label>
                        <div class="right fl">
                            <selected :list="taskTypeL"
                                :class="{'error':isTaskType,'disable':taskId}"
                                :value.sync="taskTypeId"
                                :name="'attributes.ZH'"
                                :sign="'id'"
                                v-on:unfold="getTaskTypeL"
                                v-on:chose="choseTaskType"
                                ref="taskType"
                                :placeholder="taskTypeName"></selected>
                            <div class="validBox" v-show="isTaskType">
                                <span class="line"></span>
                                <span class="fill"></span>
                            请选择任务类型</div>
                        </div>
                    </div>

                    <div class="half_row fl">
                        <label class="left fl labelColor">任务模板:</label>
                        <div class="right fl">
                            <selected :list="taskTempL"
                                :value.sync="taskTempId"
                                :name="'taskTempName'"
                                :sign="'ttId'"
                                :class="{'disable':!taskTypeId||taskId}"
                                v-on:unfold="getTaskTempL"
                                v-on:chose="choseTaskTemp"
                                ref="taskTemp"
                                :placeholder="taskTempName"></selected>
                        </div>
                    </div>
                    <div class = "tasktemp_cont fl" v-show="isTaskTemp">

                    <div class="tasktemp_detail_cont clearBox">
                        <div class="tasktemp_meun" @click="isfold = !isfold">
                            <span class="fold" :class="{'unfold':isfold}"></span>
                            任务模板详情
                        </div>
                        <div v-show="isfold" class="tasktemp_detail clearBox">
                            <div class="half_row row_slt fl">
                                <label class="left fl labelColor" >模板文档:</label>
                                <div class="right fl">
                                    <div class="doc_box" :class="{'error_border':isTempdoc}">
                                        <a :href="'/browse/onLineBrowse/getBrowseUrl.json?nodeId='+tempdocId" target="_blank" class="inputbox" :title="tempdocName">{{tempdocName}}</a>
                                        <span class="clear_btn" @click="cleartempdoc" v-if="tempdocName && isSelTempdocName">X</span>
                                        <span class="select_span select_knowledge nowrap"  @click="showDocDialog('0')" v-if="isSelTempdocName" title="选择知识库文档">选择知识库文档</span>
                                        <label class="select_span select_local nowrap"  @click="" v-if="isSelTempdocName" for = "fileChange" title="选择本地文档">选择本地文档</label>
                                        <input type = "file" id = "fileChange" @change = "fileChange($event)" ref = "inputer" style="display:none" accept='application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document'>
                                    </div>
                                    <p class="tip notNewLine">提示：模板文档只能是IDP文档，如果选择本地文档，只能选择Word文档。</p>
                                </div>
                            </div>
                            <div class="row fl"></div>
                            <div class="half_row row_slt fl">
                                <label class="left fl labelColor">设计向导:</label>
                                <div class="right fl">
                                    <div class="doc_box">
                                        <a :href="'/browse/onLineBrowse/getBrowseUrl.json?nodeId='+docAssistant" target="_blank" class="inputbox">{{docName}}</a>
                                        <span class="clear_btn" @click="cleardoc" v-if="docName">X</span>
                                        <span class="select_span select-btn" @click="showDocDialog('1')">选择</span>
                                    </div>
                                </div>
                            </div>
                            <div class="row fl">
                                <label class="left fl labelColor">搜索关键字:</label>
                                <div class="right fl">
                                    <input type="text" v-model="searchWords"
                                        @blur="keywordsBlur"
                                        :class="{'error_border':iskeywords}" >
                                    <div class="validBox" v-show="iskeywords">
                                        <span class="line"></span>
                                        <span class="fill"></span>
                                        搜索关键字不能超过100个字符
                                    </div>
                                    <p class="tip">提示：默认搜索关键字配置，可输入多个，使用空格分隔。</p>
                                </div>
                            </div>

                            <div class="row fl">
                                <label class="left fl labelColor">相关资料:</label>
                                <div class="right fl">
                                    <p class="select_p"><span class="select_span" @click="showRelatedDocDialog()" :title="'最多可再选择'+mostChose+'个相关资料'">选择</span></p>
                                    <div class="relInfor table" v-show="relateItems.length != 0">
                                        <el-table
                                            :data="relateItems"
                                            tooltip-effect="dark"
                                            style="width: 100%; max-height:300px;"
                                            >
                                            <el-table-column
                                              label="资料名称" width="300">
                                              <template slot-scope="scope">
                                                <el-popover trigger="hover" placement="right-end">
                                                  <p>{{ scope.row.name }}</p>
                                                  <div slot="reference" class="name-wrapper">
                                                    <span>
                                                        <a class="infoName" :href="'/browse/onLineBrowse/getBrowseUrl.json?nodeId='+scope.row.id" target="_blank">{{ scope.row.name }}</a>
                                                    </span>
                                                  </div>
                                                </el-popover>
                                              </template>
                                            </el-table-column>

                                            <el-table-column
                                                label="参考说明">
                                                <template slot-scope="scope">
                                                    <input type="text"  maxlength='1000' v-model = "scope.row.des" />
                                                </template>
                                            </el-table-column>
                                            <el-table-column
                                                label="Operation"
                                                width="120">
                                                <template slot-scope="scope">
                                                    <div class="operate_td">
                                                        <span title="删除" class="span_btn" @click="delRelatedDoc(scope.row)">
                                                            <img src="../../assets/delete-btn.png"></img></span>
                                                    </div>

                                                </template>
                                            </el-table-column>
                                        </el-table>

                                    </div>
                                </div>
                            </div>

                            <div class="row fl">
                                <label class="left fl labelColor">辅助工具:</label>
                                <div class="right fl">
                                    <div class="tools">
                                        <div class="domain">
                                            <span v-for="item in domainList" @click="getTools(item.id)" v-bind:class="{'active':domainId==item.id}">{{item.attributes && item.attributes.ZH}}
                                            </span>
                                        </div>
                                        <div class="checkboxall">
                                            <el-checkbox :indeterminate="isIndeterminate" v-model="ischeck" @change="checkTools()">全选</el-checkbox>
                                        </div>
                                        <el-checkbox-group v-model="assisantTools">
                                                <el-checkbox v-for="item in toolList" :key="item.id" :label="item.id" @change="checkinlist(item.id)" :title = "item.attributes && item.attributes.ZH">
                                                    {{item.attributes && item.attributes.ZH}}
                                                </el-checkbox>
                                        </el-checkbox-group>

                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    </div>

                    <!--交付件属性配置 -->
                    <div class="clearBox" v-show="isDeliverable">
                        <h4 class="menu_h4">交付件属性配置</h4>
                        <div class="half_row fl">
                            <label class="left fl required labelColor">交付件名称:</label>
                            <div class="right fl">
                                <input placeholder="必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; | \ /"
                                    type="text"
                                    maxlength="200"
                                    v-model.trim="deliverName"
                                    onfocus="this.placeholder=''"
                                    onblur="placeholder='必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; \| \\ \/'"
                                    @blur="deliverBlur"
                                    :class="{'error_border':isDeliver}" >
                                <div class="validBox" v-show="isDeliver">
                                    <span class="line"></span>
                                    <span class="fill"></span>
                                必填，长度小于200个字符，不能包含以下字符 *  :  ? &quot; &lt; &gt; | \ /</div>
                            </div>
                        </div>
                        <div class="half_row fl">
                            <label class="left fl required labelColor">交付件类型:</label>
                            <div class="right fl">
                                <selected :list="deliverableTypeL"
                                    :value.sync="deliverableType"
                                    :name="'attributes.ZH'"
                                    :sign="'id'"
                                    ref="deliverableType"
                                    v-on:unfold="getDeliverableType"
                                    v-on:chose="choseDeliverableType"
                                    :class="{'disable':!isDeliverableType,'error':isDeliverableTypeError}"
                                    :placeholder="deliverableTypeName"></selected>
                                <div class="validBox" v-show="isDeliverableTypeError">
                                    <span class="line"></span>
                                    <span class="fill"></span>
                                      请选择交付件类型
                                </div>
                            </div>
                        </div>
                        <div class="half_row row_slt row_sltDoc fl">
                            <label class="left fl labelColor">产品:</label>
                            <div class="right fl">
                                <div class="doc_box">
                                    <span class="inputbox">{{productName}}</span>
                                    <span class="clear_btn" @click="clearPro" v-if="productName">X</span>
                                    <span class="select_span select-btn" @click="selectProduct">选择</span>
                                </div>
                            </div>
                        </div>
                        <div class="half_row fl">
                            <label class="left fl labelColor">VR版本:</label>
                            <div class="right fl">
                                <selected :list="vrList"
                                          :value.sync="vrId"
                                          :name="'cn'"
                                          :sign="'id'"
                                          v-on:unfold="getVr"
                                          v-on:chose="choseVr"
                                          :class="{'disable':!productId,'error':isVersion}"
                                          ref="vrVersion"
                                          :placeholder="vesVrName"
                                          ></selected>
                            </div>
                        </div>

                        <div class="half_row fl">
                            <label class="left fl labelColor">C版本:</label>
                            <div class="right fl">
                                <selected :list="cList"
                                          :value.sync="cId"
                                          :name="'cn'"
                                          :sign="'id'"
                                          v-on:unfold="getC"
                                          v-on:chose="choseC"
                                          :class="{'disable':!vrId}"
                                          ref="cVersion"
                                          :placeholder="vesCName"
                                          ></selected>
                            </div>
                        </div>
                        <div class="half_row fl" v-show="projectCategory != '1'">
                            <label class="left fl labelColor">浏览权限:</label>
                             <div class="right fl" v-if="!this.projectId">
                                <div class="select_div">
                                <jobNumber ref="Permission"
                                    :class="{'errorBorder':isPermissions}"
                                    type="input"
                                    maxlength="50"
                                    :sign.sync="Permission"
                                    v-on:blurEvent="PermissionsBlur"
                                    ></jobNumber>
                                    <div class="validBox" v-show="isPermissions">
                                        <span class="line"></span>
                                        <span class="fill"></span>
                                    最多输入10个以;分隔</div>
                            </div>
                            </div>
                            <div class="right fl" v-if="this.projectId">
                            <select   v-model = "Selected" style="width:100px; border:1px solid #ccc;"  @change="changePermissions">
                                <option v-for = "item in changeStatus" :value = "item.id">{{item.name}}</option>
                            </select>
                            <div class="select_div" v-show="false">
                                <jobNumber ref="Permission"
                                    :class="{'errorBorder':isPermissions}"
                                    type="input"
                                    maxlength="50"
                                    :sign.sync="Permission"
                                    v-on:blurEvent="PermissionsBlur"
                                    ></jobNumber>
                                    <div class="validBox" v-show="isPermissions">
                                        <span class="line"></span>
                                        <span class="fill"></span>
                                    最多输入10个以;分隔</div>
                            </div>
                            <!-- //<input   style="width:255px;margin-left:5px" :disabled="isDisabled"> -->
                            <div  class="sel_div Seledrop_info" v-show="!isDisabled">
                                <button class="sele toggle_info" expand="false" >{{Permission?Permission:'---请选择---'}}</button>
                                <ul class="sele-menu">
                           <!--          <li><el-checkbox v-model="checkAll" @change="selectAll">All</el-checkbox></li> -->
                                    <el-checkbox-group v-model="checkedArr" @change="oneChecked">
                                        <el-checkbox v-for="item in groupsArr"  :label="item.id" :key="item.id" :title="item.name" >{{item.name}}</el-checkbox>
                                    </el-checkbox-group>
                                </ul>
                            </div>
                            </div>
                        </div>

                        <div class="half_row fl">
                            <label class="left fl labelColor">关键字:</label>
                            <div class="right fl">
                                <div class="right fl11111" style="width:100%;" @click.stop = "changeTolabel(deliverkeywords)">
                                    <input type="text"
                                           style="width:365px"
                                           maxlength="2000"
                                           id = "keyword"
                                           value = ""
                                           v-model.trim="deliverkeywords"
                                           ref="autocomplete"
                                           placeholder="字符数少于20个,且不能包含以下字符 *  : ; ? , &quot; &lt; &gt; | \ /"
                                           v-if = "!this.showlabel"
                                           :trigger-on-focus="false"
                                           @keydown.enter = "keydownEvent($event,deliverkeywords)"
                                           @blur="Blur($event)"
                                    />
                                    <div  class="label"  v-show = "showlabel">
                                            <div class = "labelbox"
                                                v-for="(item,index) in this.labels"
                                                :key="index"
                                                >
                                                <a ><span class="textlabelsapn" :title="item">{{item}}</span> <img src="../../assets/tab_close.png" @click.stop = "dellable(item,index,labels)"></a>
                                            </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="row fl">
                            <label class="left fl labelColor">摘要:</label>
                            <div class="right text_padding fl">
                                <textarea maxlength="2000" v-model="deliverDes"></textarea>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- 提交 -->
                <div class="btn_cont fl">                  
                    <button class="btn btn-light" @click="submitTask()">确定</button>
                    <button class="btn btn-normal" @click="cancletemp">取消</button>
                </div>
            </div>

        </el-dialog>

        <!--  文档助手/模板文档弹窗 -->
        <docDialog v-on:getdoc = "getdoc" ref="doc"></docDialog>
        <!-- 相关资料弹窗 -->
        <relatedDocDialog ref="relateddoc" :relatedList="relatedList" @multiCaseChosen = 'addRelatdDoc'></relatedDocDialog>
        <!-- 产品 -->
        <productPbi v-model = "productScopeDialog" ref = "product" @choseProduct = "choseProduct" :num = "num"></productPbi>
        <!--作者输入有误提醒-->
        <el-dialog :visible.sync="showOwnerErr" size="tiny" >
            <div style="padding: 0 10px;">以下人员信息有误，请更改。</div>
            <ul class="text_over" style="word-wrap: break-word;padding: 0 25px;">
                <li v-for="(item,index) in errOwners">{{item+";"}}</li>
            </ul>
            <span slot="footer" class="dialog-footer btn_cont">
                <div class="btn_cont">
                    <button class="btn btn-light" @click="showOwnerErr=false">关闭</button>
                </div>
            </span>
        </el-dialog>
    </div>
</template>

<script>
    import '../../style/project.css'
    import docDialog from '../tasktemplate/docDialog.vue'
    import relatedDocDialog from '../tasktemplate/relatedDocDialog.vue'
    import productPbi from '../assemblies/productPBI.vue'
    import givenNumber from '../assemblies/givenNumber.vue'
    //引进非父子组件控制器
    import Bus from '../assemblies/bus.js'
    export default{
        components:{productPbi,docDialog, relatedDocDialog,givenNumber},
        data(){
            return {
                projectCategory:'',
                groupsIds:'',
                checkedAll:false,
                checkedArr:[],
                groupsArr:[],
                //单个标签
                label:"",
                //标签数组
                labels:[],
                showlabel:true,
                dialogName:'创建任务',
                taskDialog:false,
                //是否可以选择模板文档，默认为true
                isSelTempdocName:true,
                //
                titleName:'创建任务',
                isDeliverable: false,
                //名称
                title:"",
                isTitle:false,
                //责任人
                isOwner:false,
                owner:'',
                showOwnerErr:false,
                isPermissions:false,
                isTaskTemp:false,
                isDeliverableTypeError:false,
                modelId:'',
                isDeliverableType:false,
                deliverableType:'',
                deliverableTypeL:[],
                deliverableTypeName:'---请选择交付件类型---',
                Permission:'',
                PermissionIds:'',
                errOwners:'',
                //任务说明
                taskDes:"",
                isDefaultdocTask:false,
                //计划完成时间
                planTime:'',
                isPlanTime:false,
                pickerLimit: {
                    disabledDate(time) {
                        return time.getTime() <  Date.now();
                    }
                },
                //任务类型
                taskTypeL:[],
                taskTypeId:"",
                taskTypeName:'---请选择任务类型---',
                isTaskType:false,
                //任务模板
                taskTempL:[],
                taskTempId:'',
                taskTempName:'---请选择任务模板---',
                //文档助手
                docAssistant:"",
                docName:"",
                //模板
                tempdocId:"",
                tempdocName:"",
                isTempdoc:false,
                //模板详情关键字
                searchWords:"",
                // type = 0  打开模板文档窗口，1为打开设计助手窗口
                type:'',
                //是否折叠模板选择 默认展开
                isfold:true,
                //辅助工具
                toolList:[],
                //已选的工具
                assisantTools:[],
                model : {},
                taskInfo : {
                    "id": "",
                    "taskName" : "",
                    "taskType" : "",
                    "status": "0",
                    "owner": "",
                },
                //产品选择
                productScopeDialog:false,
                num:1,
                productId:'',
                productName:"",
                //vr版本
                vesVrName:"---请选择VR版本---",
                vrList:[],
                vrId:'',
                //c版本
                cList:[],
                vesCName:"---请选择C版本---",
                cId:'',
                //交付件
                nodeId:'',
                deliverName:'',
                //交付件关键字
                deliverkeywords:'',
                //交付件摘要
                deliverDes:'',
                isDeliver:false,

                //相关资料(文档和参考说明的List),singlerelateDoc组成的数组，用于生产JSOn串
                relatedDoc:[],
                //相关资料的文档List,用于本页展示
                relateItems:[],
                //用于传递给子组件
                relatedList:[],
                //最大可选数量
                mostChose:100,
                // 编辑时用来缓存相关资料的数组
                relatedCopyList:[],

                //任务ID
                taskId:"",
                //交付表主键Id
                dleId:"",
                authorized:false,
                power:'',
                //项目id
                projectId:"",
                //项目名称
                projectName:"",
                Selected:4,
                isDisabled:true,
                changeStatus:[
                    {
                        "id":1,
                        "name":"内部公开"
                    },
                    {
                        "id":4,
                        "name":"项目公开"
                    },
                    {
                        "id":3,
                        "name":"指定组可见"
                    },
                    {
                        "id":2,
                        "name":"指定人可见"
                    },
                ],
                pid:'',
                supId:'',
                weight:'',
                iskeywords:false,
                isTaskdes:false,
                taskTypeTerm:'',
                planTimeName:"",
                fileItems:[],
                ischeck:true,
                //故障工具新增领域
                domainList:[],
                domainId:"",
                allassisantTools:[],
                isIndeterminate:false 
            }
        },
        created:function(){
            this.isAuthorized();
            this.projectId = this.$route.params.id;
            //this.getGroups();
        },
        mounted:function(){
            $(window).on('click',function(e){
                $('.Sel_permtype').removeClass('open');
                $('.toggle_permtype').attr('expand','false');

                $('.Seledrop_info').removeClass('open');
                $('.toggle_info').attr('expand','false');
            });


            $('#app').on('click','.toggle_permtype',function(e){
                var target = e.target;
                $('.Seledrop_info').removeClass('open');
                if( $('.Seledrop_info').find(".toggle_info")[0]){
                   $('.Seledrop_info').find(".toggle_info")[0].setAttribute('expand','false');
                }
                $('.Seledrop_info').removeClass('open');
                $('.toggle_info').attr('expand','false');


                $('.Sel_permtype').removeClass('open');
                $('.toggle_permtype').attr('expand','false');

                e.target.parentNode.classList.add('open');
                e.target.setAttribute('expand','true');
                e.stopPropagation();



            })

            $('#app').on('click','.Seledrop_info',function(e){
                if($('.Sel_permtype').find(".toggle_permtype")[0]){
                    $('.Sel_permtype').find(".toggle_permtype")[0].setAttribute('expand','false');
                }
                $('.Seledrop_info').removeClass('open');
                $('.toggle_info').attr('expand','false');

                $('.Sel_permtype').removeClass('open');
                $('.toggle_permtype').attr('expand','false');
                e.currentTarget.classList.add('open');
                e.currentTarget.firstChild.setAttribute('expand','true');
                e.stopPropagation();
            })
            

        },
        methods:{
        clearpermission(){
            let self = this;
            self.groupsArr = [];
            self.checkedArr = [];
            self.taskDialog = false;
        },
        //选中一个
        oneChecked(value) {
            this.Permission = value;
            this.getCheckedNames();
        },
        dltEmptyEl(arr){
            var res = [];
            for(var i=0; i<arr.length; i++){
                if(arr[i]){
                    res.push(arr[i]);
                }
            }
            return res;
        },
        //获取组
        getGroups(){
            let self = this;
            this.$http.get("/project/group/getProjectGroups.json",{
                params:{
                    projectId:this.projectId
                }
            }).then(res=>{
                res = res.data;
                if(res.body.message && res.body.message.length){
                    self.groupsArr = res.body.message;
                }else{
                    self.groupsArr = [];
                }
                // if(self.groupsArr.length <= 0){
                //     self.changeStatus = [
                //     {
                //         "id":1,
                //         "name":"内部公开"
                //     },
                //     {
                //         "id":4,
                //         "name":"项目公开"
                //     },
                   
                //     {
                //         "id":2,
                //         "name":"指定人可见"
                //     },];
                // }
                if(self.projectCategory == '0'){
                    if(self.groupsArr.length <= 0){
                            self.changeStatus = [
                        {
                            "id":1,
                            "name":"内部公开"
                        },
                        {
                            "id":4,
                            "name":"项目公开"
                        },
                       
                        {
                            "id":2,
                            "name":"指定人可见"
                        },];
                    }
                }else{
                    if(self.groupsArr.length <= 0){
                        self.changeStatus = [
                        {
                            "id":4,
                            "name":"项目公开"
                        },
                       
                        {
                            "id":2,
                            "name":"指定人可见"
                        },];
                    }else{
                         self.changeStatus=[
                        {
                            "id":4,
                            "name":"项目公开"
                        },
                        {
                            "id":3,
                            "name":"指定组可见"
                        },
                        {
                            "id":2,
                            "name":"指定人可见"
                        },]
                    }
                }
            })
        },
        //获取人员
        getGroupMembers(groupsIds){
            let self = this;
            this.$http.post("/project/member/getProjectGroupMembers.json",{
                'projectId':this.projectId,
                'groups':groupsIds,
            }).then(res=>{
                res = res.data;
                if(res.body.message && res.body.message.length){
                    self.groupsArr = res.body.message;
                    self.getMemerCheckedNames();
                }else{
                    self.groupsArr = [];
                    //self.updatePerm(self.permType.value,'');
                }
            })
        },
        //获取选中的组
        getCheckedNames(){
            this.Permission ="";
            for(var i =0;i< this.groupsArr.length;i++){
                for (var j=0;j< this.checkedArr.length;j++) {
                    if(this.groupsArr[i].id == this.checkedArr[j]){
                        this.Permission += this.groupsArr[i].name+';';
                        console.log("this.Permission",this.Permission);
                    }
                };
            }
            if(this.checkedArr.length == this.groupsArr.length){
                this.checkAll = true;
            }
        },
        //获取选中的成员
        getMemerCheckedNames(){
            this.Permission ="";
            for(var i =0;i< this.groupsArr.length;i++){
                for (var j=0;j< this.checkedArr.length;j++) {
                    if(this.groupsArr[i].id == this.checkedArr[j]){
                        this.Permission += this.groupsArr[i].name+';';
                        console.log("this.Permission",this.Permission);
                    }
                };
            }
            if(this.checkedArr.length == this.groupsArr.length){
                this.checkAll = true;
            }
        },

            //全部选中以及全部不选中切换
            checkTools(){
                let self = this;
                self.assisantTools=[];
                if(!self.ischeck){
                    self.ischeck=true;
                }else{
                    self.ischeck=false;
                    self.toolList.forEach(function(item){
                                self.assisantTools.push(item.id);
                            })
                }
                self.checkassistant();
            },
            //初始化是否全部选中取消
            checkassistant(){
                let  self = this;
                if(self.assisantTools.length>0 && self.assisantTools.length==self.toolList.length){
                    self.ischeck=true;
                    self.isIndeterminate=false;
                }
                else if(self.assisantTools.length==0){
                    self.ischeck=false;
                    self.isIndeterminate=false;
                }else{
                    self.isIndeterminate=true;
                }
            },
            // 删除标签
            dellable(lable,num,items){
                if(items && items.length > 0){
                    items.forEach(function(item,index){
                        if(index == num){
                            items.splice(num,1);
                        }
                    });
                    this.deliverkeywords = items.join(';');
                }
            },

            //点击标签转换为字符串
            changeTolabel(deliverkeyword){
                var self = this;
                this.showlabel = false;
                //自动聚焦
                this.$nextTick(() => {
                    this.$refs['autocomplete'].focus()
                });
                this.deliverkeywords = "";
                this.labels.forEach(item => {
                    this.deliverkeywords += item + ';';
                });
            },

            //清空标签
            Blur(event,deliverkeywords){
                let self = this;
                setTimeout(function(){
                    if(self.labels.length > 0 ){
                        self.showlabel = true;
                    }
                }, 100);
            },

            // 按回车键后
            keydownEvent(event,deliverkeywords){
                let self = this;
                var keyVal = event.keyCode;//键值
                // 回车键
                if(keyVal == 13){
                    var reg = /^[^*\/|:<>?,\\"]*$/;
                    let len = deliverkeywords.split(';').length;
                    self.label = deliverkeywords.split(';')[len - 1 ];
                    var curdeliverkeywords = this.labels.join(';');
                    if (!self.label) {
                        D.showMsg("请输入内容");
                        return;
                    };
                    if(!self.label || !reg.test(self.label)){
                        this.deliverkeywords = curdeliverkeywords;
                        D.showMsg("不能包含以下字符 *  : ; ? , &quot; &lt; &gt; | \ /");
                        return;
                    }
                    if(self.label.length > 20){
                        this.deliverkeywords = curdeliverkeywords;
                        D.showMsg("标签长度不能超过20");
                        return;
                    }
                    // 去重
                    if(self.label){
                        if(this.labels.length > 4){
                            this.deliverkeywords = curdeliverkeywords;
                            D.showMsg("标签最多为5个。");
                            return;
                        }
                        for(var i = 0; i < this.labels.length; i++){
                            if(self.label == this.labels[i]){
                                this.deliverkeywords = curdeliverkeywords;
                                D.showMsg("标签重复，请重新输入");
                                return;
                            }
                        }
                        this.labels.push(self.label);
                        this.showlabel = true;
                    }
                }
            },

            initData(){
                this.getTaskTypeL();
            },
            defaultSelTaskType() {
                let self=this;
                self.taskTypeL.forEach(function(item){
                    if(item.id=='PROJECT-customTask'){
                        self.taskTypeName=item.attributes.ZH;
                        self.$refs.taskType.choseName=self.taskTypeName;
                        self.choseTaskType(item);
                        self.taskTypeId='PROJECT-customTask';
                    }
                });
            },
            resetAll(type){
                //初始化辅助工具
                if(type=='1'){
                    this.getdomainbyAuxiliary([]);
                }
                //自动聚焦
                this.$nextTick(() => {
                    this.$refs['taskName'].focus()
                });
                let self =this;
                this.isDeliverable = false;
                this.isTaskTemp = false;
                this.isSelTempdocName = true;
                //清空标题等
                this.title='';
                this.planTime='';
                this.taskDes='';
                this.isTitle=false;
                this.isTaskType = false;
                this.isDefaultdocTask = false;
                this.isDeliver = false;
                this.isDeliverableTypeError = false;
                //清空任务类型
                this.taskTypeL =[];
                this.taskTypeId ='';
                this.taskTypeName = '---请选择任务类型---';
                this.$nextTick(function(){
                    this.$refs.taskType.placeholder = this.taskTypeName;
                    this.$refs.taskType.reset();
                });
                //清空任务模板
                this.taskTempL =[];
                this.taskTempId ='';
                this.taskTempName = '---请选择任务模板---';
                this.$nextTick(function(){
                    this.$refs.taskTemp.placeholder = this.taskTempName;
                    this.$refs.taskTemp.reset();
                });
                //清空模板文档
                this.cleartempdoc();
                //清空文档助手
                this.cleardoc();
                this.searchWords='';
                //清空pbi
                this.productName = '';
                this.productId = '';
                this.labels = [];
                this.deliverkeywords = '';
                //清空浏览权限
                this.Selected = 4;
                this.isDisabled=true;
                this.PermissionIds = "";
                this.Permission='';
                this.dleId = '',
                this.modelId = '',
                this.nodeId = '';
                if(this.taskId){
                    this.planTimeName = "";
                }
                else{
                    this.planTimeName = "请选择日期";
                }
                //重置所有版本信息
                self.restversion();
                self.choseTaskType();
            },
            submitTask(){
                let self = this;
                for (var i in self.allassisantTools){
                    var key =  Object.keys(self.allassisantTools[i])[0];
                    if(self.domainId==key)
                    {
                        self.allassisantTools[i][key]=self.assisantTools;
                    }
                }
                D.block();
                self.checkedArr = [];
                self.groupsArr = [];
                this.PermissionsBlur();
                setTimeout(function(){
                    self.mysubmitTask();
                },1000);
            },
            //提及任务
            mysubmitTask(){
                let self = this;
                this.titleBlur();
                if(this.isTitle){
                    D.unblock();
                    return;
                }
                this.ownerBlur();
                if(this.isOwner){
                    D.unblock();
                    this.isOwner = true;
                    return
                }
                if(!this.taskTypeId){
                    D.unblock();
                    this.isTaskType = true;
                    return
                }
                //isDeliverable 为真为写作任务 模板文档不能为空
                if(this.isDeliverable){
                    this.deliverBlur();
                    if(this.isDeliver) {
                        D.unblock();
                        return;
                    }
                    if (!this.deliverableType) {
                        D.unblock();
                        this.isDeliverableTypeError = true;
                        return;
                    }
                }
                //组装relatedDoc数组
                this.relatedDoc = [];
                this.setRelateDoc();
                console.log("this.PermissionIds  2222",this.PermissionIds);
                if(self.projectCategory == '2'){
                    if(this.Selected == '1'){
                      this.power = 'ALL';
                    }else if(this.Selected == '4'){
                      this.power = 'PDIS';
                    }else{
                        if(!this.PermissionIds.length){
                            this.power = 'PDIS';
                        }else{
                            this.power = this.PermissionIds;
                        }
                    }
                }else if(self.projectCategory == '0'){
                    if(this.Selected == '1'){
                      this.power = 'ALL';
                    }else if(this.Selected == '4'){
                      this.power = 'PDIS';
                    }else{                     
                        this.power = this.PermissionIds;
                    }
                }else{
                    // if(this.Selected == '4'){
                    //   this.power = 'PDIS';
                    // }else{                     
                    //     if(!this.PermissionIds.length){
                    //         this.power = 'PDIS';
                    //     }else{
                    //         this.power = this.PermissionIds;
                    //     }
                    // }
                    this.power = D.sysUid;
                }
                
                 //任务基本属性
                var taskInfo = {
                    weight:this.weight?this.weight:'',//权重
                    supTaskId:this.supId?this.supId:'',//父Id
                    projectId:this.projectId?this.projectId:'',//项目Id
                    taskId:this.taskId?this.taskId:'',     //任务ID
                    taskName:this.title,     //任务名称
                    owner:this.owner,     //责任人,多个以;分割开
                    status:this.status,     //任务状态  0:待分配 1:进行中  2:已完成 3:删除
                    taskType:this.taskTypeId,     //任务类型ID
                    taskTemplateId:this.taskTempId,     //任务模板id
                    description:this.taskDes,     //任务说明
                    docAssistant:this.docAssistant,     //文档助手
                    templateDoc:this.tempdocId,     //模板文档NodeId
                    searchWords:this.searchWords,     //搜索关键字
                    relatedDocDesc:this.relatedDoc,     //相关资料及参考说明，以JSON格式存入
                    auxiliaryTools:this.allassisantTools,
                     //辅助工具，元数据ID,分号隔开
                    finishTime:this.planTime,     //完成时间
                    //交付文档Id
                    nodeId:this.nodeId,

                    //交付件属性
                    deliverable:{
                        id:this.dleId,
                        deliverName:this.deliverName,
                        authorized:this.power,
                        deliverableType:this.deliverableType,
                        modelId:this.modelId,
                        product:this.productId,
                        vrVersion:this.vrId,
                        cVersion:this.cId,
                        keywords:this.deliverkeywords,
                        description:this.deliverDes,
                    },
                    fileItems:this.fileItems
                }
                self.veriOwner(taskInfo);
            },
            //责任人校验
            veriOwner(taskInfo){
                let self = this;
                var userIds = taskInfo.owner;
                //验证人员信息
                D.veriUser({
                    userIds:D.jobNumberStrSpace(userIds),
                    success(){
                        self.taskQuery(taskInfo);
                    },
                    error(arr){
                        D.unblock();
                        if(arr){
                            self.showOwnerErr = true;
                            self.errOwners = arr;
                        }else{
                            D.showMsg();
                        }
                    }
                })
            },

            //任务请求
            taskQuery(taskInfo){
                let self = this;
                let url = "",tipMng="";
                if(taskInfo.taskId){
                    url= '/project/taskextension/update.json';
                    tipMng ="修改成功！"
                }else{
                    url = '/project/taskextension/create.json';
                    tipMng ="创建成功！"
                }
                D.block();
                this.$http.post(url, taskInfo, {
                    contentType:'application/json',
                })
                .then(function(res){
                    D.unblock();
                    res=res.data;
                    if(res.head.flag){
                         self.taskDialog =false;
                         //新增任务，刷新一级任务列表
                        if(taskInfo.taskId == self.$route.params.id || taskInfo.supTaskId == self.$route.params.id)
                        {
                            Bus.$emit('addTask',taskInfo);
                        }
                        else if(taskInfo.supTaskId)
                        {
                            Bus.$emit('addSubTask',taskInfo);
                        }
                        // else if(taskInfo.supTaskId && taskInfo.taskId)
                        // {
                        //     Bus.$emit()
                        // }

                        //刷新任务状态数量
                        Bus.$emit("refreshTaskStatusCount",taskInfo.taskId ? "updateTask" : "createTask");

                    }else{
                        //错误码
                        if(res.head.tips == "dfx.projectwebsite.task.exist.task.name"){
                            D.showMsg("任务重名,请修改！");
                        }else if (res.head.tips == "dfx.projectwebsite.task.exist.dle.name") {
                            D.showMsg("交付件重名,请修改！");
                        }else if (res.head.tips == "dfx.projectwebsite.create.task.failed") {
                            D.showMsg("创建任务失败！");
                        }else if (res.head.tips == "dfx.projectwebsite.update.task.failed") {
                            D.showMsg("更新任务失败！");
                        }else if (res.head.tips == "dfx.system.model.error") {
                            D.showMsg("Model信息配置错误，请联系管理员！");
                        }else if(res.head.tips == "dfx.project.task.idpList.is.null"){
                            D.showMsg("任务的idp文档创建中，不能编辑责任人。");
                        }else {
                            D.showMsg("提交失败!");
                        }
                    }

                })
                .catch(function(){
                    D.unblock();
                    D.showMsg('创建失败,网络服务异常,请联系管理员!');
                });
            },
            //删除相关资料
            delRelatedDoc(item){
                // console.log("item",item);
                var index = this.relateItems.indexOf(item);
                this.relateItems.splice(index,1);
                // console.log("this.caseItems",this.caseItems);
                this.mostChose = 100 - this.relateItems.length;
            },
            addRelatdDoc(list){
                //let _this = this;
                this.relateItems = list;
                if (this.relatedCopyList.length > 0) {
                    for (var i = 0; i < this.relateItems.length; i++) {
                        for (var j = 0; j < this.relatedCopyList.length; j++) {
                            if (this.relateItems[i].id == this.relatedCopyList[j].id) {
                                this.relateItems[i].des = this.relatedCopyList[j].des;
                            }
                        }
                    }
                }
                for (var i = 0; i < this.relateItems.length; i++) {
                    for (var j = 0; j < this.relatedCopyList.length; j++) {
                        if (this.relateItems[i].id == this.relatedCopyList[j].id) {
                            this.relateItems[i].des = this.relatedCopyList[j].des;
                        }
                    }
                }
                this.mostChose = 100 - list.length;
                if (this.mostChose < 0) {
                    D.showMsg("最多可选择100个相关资料");
                }
                this.$refs.relateddoc.relatedDocDialog = false;
                this.$refs.relateddoc.relatedList = [];
            },
            //选择相关资料
            showRelatedDocDialog(){
                let self = this;
                self.relatedList = self.relateItems;
                this.$refs.relateddoc.relatedDocDialog = true;
                setTimeout(function(){
                    self.$refs.relateddoc.setRelatedList();
                },500);
            },
            //组装relatedDoc数组
            setRelateDoc(){
                let self = this;
                let ris = this.relateItems;
                if (ris && ris.length > 0) {
                    for (let i = 0; i <= ris.length - 1; i++) {
                        var sdoc ={
                            nodeId:"",
                            referenceDesc:"",
                        }
                        sdoc.nodeId = ris[i].id;
                        sdoc.referenceDesc = ris[i].des;
                        self.relatedDoc.push(sdoc);
                    }
                }
            },
            //产品选择
            choseProduct(value){
                this.productName = value.text;
                this.productId = value.productIds[0];
                this.restversion();
            },
            //重置所有版本信息
            restversion(){
                this.cList=[];
                this.vrList=[];
                this.vrId="";
                this.cId="";
                this.vesVrName = "---请选择VR版本---";
                this.vesCName = "---请选择C版本---";
                this.$nextTick(function(){
                    this.$refs.vrVersion.placeholder = "---请选择VR版本---";
                    this.$refs.cVersion.placeholder ="---请选择C版本---";
                    this.$refs.vrVersion.reset();
                    this.$refs.cVersion.reset();
                });
            },
            //获取vr版本
            getVr(){
                const self = this;
                D.block();
                this.$http.get("/project/pbi/org/edition/vr/offering.json",{
                    params:{"sourceId":self.productId}})
                .then(function(res){
                    D.unblock();
                    res = res.data;
                    if(res.body.pbiNodes){
                        self.vrList = res.body.pbiNodes;
                    }else{
                        self.vrList = [];
                    }
                })
                .catch(function(){
                    D.unblock();
                    D.showMsg();
                });
            },
            //获取c版本
            getC(){
                const self = this;
                D.block();
                this.$http.get("/project/pbi/org/edition/c/vr.json",{
                    params:{"sourceId":self.vrId}})
                .then(function(res){
                    D.unblock();
                    res = res.data;
                    if(res.body.pbiNodes){
                        self.cList = res.body.pbiNodes;
                    }else{
                        self.cList = [];
                    }
                })
                .catch(function(){
                    D.unblock();
                    D.showMsg();
                });
            },
            //选择VR版本
            choseVr(item){
                this.cList=[];
                this.cId ="";
                this.ves_W="";
            },
            choseC(item){
            },
            //获取辅助工具所属领域
            getdomainbyAuxiliary(tools){
                let self = this;
                self.$http({
                    url:'/project/meta/topterms.json?'+(new Date()).getTime(),
                    method:"get",
                    params:{
                      vid:"AuxiliaryTools"
                    }
                }).then(res => {
                    console.log("res",res.data)
                    res =res.data;
                    if(res.head.flag){
                        self.domainList = res.body.message;
                        if(self.domainList.length>0)
                        {
                            self.allassisantTools = [];
                            for(var i in self.domainList)
                            {
                                var title = self.domainList[i].id;
                                var name = {};
                                name[title] =[];
                                self.allassisantTools.push(name);
                            }
                            if(tools && tools.length>0){
                                for(var k in tools)
                                {
                                    for (var y in self.allassisantTools)
                                    {
                                        var key  = Object.keys(self.allassisantTools[y])[0];
                                        var tool = Object.keys(tools[k])[0];
                                        if (key==tool)
                                        {
                                            self.allassisantTools[y][key] = tools[k][tool];
                                        }
                                    }
                                }

                            }
                            
                            self.getAssistant(self.domainList[0].id);
                        }
                       /* if(!self.assisantTools.lenght){
                            self.toolList.forEach(function(item){
                                self.assisantTools.push(item.id);
                            })
                        }*/

                    }else{
                        D.showMsg("获取辅助工具失败，请联系管理员！")
                    }

                });
            },
             getAssistant(id){
                let self = this;
                self.domainId=id;
                self.getAidedToolsByTid(id);
            },
            getTools(id){
                let self = this;
                for (var i in self.allassisantTools){
                    var key =  Object.keys(self.allassisantTools[i])[0];
                    if(self.domainId==key)
                    {
                        self.allassisantTools[i][key]=self.assisantTools;
                    }
                }
                self.domainId=id;
                self.getAidedToolsByTid(id);
            },
            //根据tid获取领域工具
            getAidedToolsByTid(id){
                let self = this;
                //self.assisantTools=[];
                //self.toolList=[];
                self.$http({
                    url:'/project/meta/getSubterms.json?'+(new Date()).getTime(),
                    method:"get",
                    params:{
                      tid:id
                    }
                }).then(res => {
                    console.log("res",res.data)
                    res =res.data;
                    if(res.head.flag){
                        self.toolList = res.body.message;
                        var index=0;
                        if(self.allassisantTools.length>0)
                        {
                            for (var i in self.allassisantTools)
                            {
                                var key =  Object.keys(self.allassisantTools[i])[0];
                                if(key==id){
                                    self.assisantTools = self.allassisantTools[i][key];
                                }
                                
                            }
                            self.checkassistant();
                        }
                        
                       /* if(!self.assisantTools.lenght){
                            self.toolList.forEach(function(item){
                                self.assisantTools.push(item.id);
                            })
                        }*/

                    }else{
                        D.showMsg("获取辅助工具失败，请联系管理员！")
                    }

                });
            },
            //选择辅助工具
            checkinlist(toolId){
                this.checkassistant();
            },
            //获取任务类型
            getTaskTypeL(){
                let self = this;
                if (self.taskTypeL &&　self.taskTypeL.length > 0) {
                    return;
                }
                D.block();
                self.$http({
                    url:'/project/meta/getSubterms.json?'+(new Date()).getTime(),
                    method:"get",
                    params:{
                      tid:"PROJECT-TASKTYPE"
                    }
                }).then(res => {
                    D.unblock();
                    // console.log("res",res.data)
                    res =res.data;
                    if(res.head.flag){
                        self.taskTypeL = res.body.message;
                        if (self.isDefaultdocTask) {
                            self.defaultSelTaskType();
                        }
                    }else{
                        D.showMsg("获取任务类型失败，请联系管理员！")
                    }
                });
            },
            //选中任务类型
            choseTaskType(type) {
                let self = this;
                this.isTaskType = false;
                console.log("type", type);
                //清除任务模板信息
                this.clearTaskTempInfo();
                //清楚交付件信息
                this.clearDeliverableConfig();
                //初始化任务模板详情
                this.initTaskTempInfo(type);

                //初始化交付件属性
                this.initDeliverableConfig(type);
            },
            //重新初始化任务模板详情
            clearTaskTempInfo() {
                //清空模板详情
                //文档助手
                this.docAssistant = '';
                this.docName =  '';
                //模板
                this.tempdocId = '';
                this.tempdocName = '';
                this.isTempdoc = false;
                //搜索关键字
                this.searchWords = '';
                //相关资料
                this.relateItems = [];
                this.relatedList = [];
                this.relatedCopyList = [];
                //已选的辅助工具
                this.assisantTools = [];
            },
            //重新初始化交付件信息
            clearDeliverableConfig() {
                //清空任务模板
                this.taskTempL =[];
                this.taskTempId ='';
                this.taskTempName = '---请选择任务模板---';
                this.$nextTick(function(){
                    this.$refs.taskTemp.placeholder = this.taskTempName;
                    this.$refs.taskTemp.reset();
                });

                //初始交付文档属性
                //交付件
                this.deliverName = '';
                //交付件关键字
                this.deliverkeywords = '';
                //交付件摘要
                this.deliverDes = '';
                this.isDeliver =false;

                //初始化交付件类型
                this.deliverableType = '';
                this.deliverableTypeL = [];
                this.deliverableTypeName = '---请选交付件类型---';
                this.$nextTick(function(){
                    this.$refs.deliverableType.placeholder = this.deliverableTypeName;
                    this.$refs.deliverableType.reset();
                });
            },
            getDeliverableType() {

            },
            //选中交付件类型
            choseDeliverableType(type) {
                this.isDeliverableTypeError = false;
            },
            initTaskTempInfo(type) {
                //显示交付件配置信息
                if (type &&　type.attributes && type.attributes.isTaskTempInfo == 'true') {
                    this.isTaskTemp = true;
                } else {
                    this.isTaskTemp = false;
                }
            },
            //初始化交付件配置信息
            initDeliverableConfig(type) {
                //显示交付件配置信息
                if (type &&　type.attributes && type.attributes.isDeliverable == 'true') {
                    this.isDeliverable = true;
                    this.isDeliverableType = true;
                    this.modelId = type.attributes.mid;
                    this.initDeliverable(type.attributes.deliverableType);
                    return;
                    //动态初始化交付件属性。代码先写死，后续需要在放开，下面代码不执行
                    this.initDeliverableAttribute(this.modelId);
                } else {
                    this.isDeliverable = false;
                }
            },
            //初始化交付件类型下拉框
            initDeliverable(deliverableType) {
                let self = this;
                if (deliverableType) {
                    self.$http({
                        url:'/project/meta/getSubterms.json?'+(new Date()).getTime(),
                        method:"get",
                        params:{
                            tid: deliverableType
                        }
                    }).then(res => {
                        console.log("res",res.data)
                        res =res.data;
                        if(res.head.flag){
                            self.deliverableTypeL = res.body.message;
                            if (!self.deliverableType) {
                                return;
                            }
                            //初始化交付件信息
                            self.deliverableTypeL.forEach(term => {
                                if (term.id == self.deliverableType){
                                    self.deliverableTypeName = term.attributes.ZH;
                                    self.$refs.deliverableType.placeholder = self.deliverableTypeName;
                                    self.$refs.deliverableType.reset();
                                    self.deliverableType = term.id;
                                    return;
                                }
                            })
                        }else{
                            D.showMsg("获取交付件类型失败，请联系管理员！")
                        }
                    });
                }
            },
            initDeliverableAttribute(mid) {
                self.$http({
                    url:'/project/model/getModelByMid.json?'+(new Date()).getTime(),
                    method:"get",
                    params:{
                      mid: mid
                    }
                }).then(res => {
                    console.log("model",res.data)
                    res =res.data;
                    if(res.head.flag){
                        self.model = res.body.message;
                    }else{
                        D.showMsg("获取任务类型失败，请联系管理员！")
                    }
                });
            },
            //获取任务模板
            getTaskTempL(){
                let self = this;
                if (!self.taskTypeId) {
                    return;
                }
                D.block();
                self.$http({
                    url:'/project/tasktemplate/getTaskTemplateByType.json?'+(new Date()).getTime(),
                    method:"get",
                    params:{
                      type: self.taskTypeId
                    }
                }).then(res => {
                    D.unblock();
                    // console.log("taskTempL",res.data)
                    res = res.data;
                    if(res.head.flag){
                        self.taskTempL = res.body.message;
                    }else{
                        D.showMsg("获取任务模板信息失败，请联系管理员！")
                    }
                });
            },
            //选择任务模板
            choseTaskTemp(){
                let self = this;
                let taskTempEx = "";
                if(!this.taskTempId ||　!this.isTaskTemp){
                    return;
                }
                D.block();
                this.$http.get("/project/tasktemplate/getTaskTemplateDetail.json?"+(new Date()).getTime(),{
                    params:{'tasktempId':self.taskTempId}})
                .then(function(res){
                    D.unblock();
                    res = res.data;
                    // console.log("模板详情",res)
                    if(res.head.flag){
                        taskTempEx = res.body.taskTemplateEx;
                        //任务类型
                        self.taskTypeId = taskTempEx.taskType;
                        self.taskTypeName = taskTempEx.templateTypeName;
                        //文档助手
                        self.docAssistant = taskTempEx.docAssistant;
                        self.docName =  taskTempEx.docAssistantName;
                        //模板文档
                        self.tempdocId = taskTempEx.templateDoc;
                        self.tempdocName = taskTempEx.templateDocName;
                        //清空选择本地文档
                        self.fileItems = [];

                        //关键字
                        self.searchWords = taskTempEx.searchKeyword;
                        //相关资料
                        self.relateItems = taskTempEx.relateItems  || [];
                        self.relatedCopyList = taskTempEx.relateItems  || [];
                        //已选的辅助工具
                        self.assisantTools = taskTempEx.assisantToolsList || [];
                    }else{
                        D.showMsg("获取任务模板失败，请联系管理员！")
                    }
                })
                /*.catch(function(){
                    D.showMsg();
                });*/
            },
            //选择模板文档、文档助手
            showDocDialog(type){
                let self = this;
                this.type = type
                this.$refs.doc.docDialog = true;
                if(this.type =="0"){
                    this.$refs.doc.id = this.tempdocId;
                    this.$refs.doc.dialogName = '模板文档';
                }else{
                    this.$refs.doc.id = this.docAssistant;
                    this.$refs.doc.dialogName = '设计向导';
                }
                this.$refs.doc.dialogType = this.type;
                this.$refs.doc.searchreset();
            },
            //回显模板文档、文档助手
            getdoc(data){
                if(this.type =="0"){
                    this.tempdocName = data.name;
                    this.tempdocId = data.id;
                    this.fileItems = [];
                    if(this.tempdocId){
                      this.isTempdoc = false;
                    }
                }else{
                    this.docName = data.name;
                    this.docAssistant = data.id;
                }

            },
            //清除模板文档
            cleartempdoc(){
                this.tempdocName = "";
                this.tempdocId = "";
                this.fileItems = [];
            },
            //清除文档助手
            cleardoc(){
                this.docName = "";
                this.docAssistant = "";
            },
            //清除产品
            clearPro(){
                this.productId = '';
                this.productName = "";
                this.restversion();
            },
            //计划完成时间
            changePick(){
                this.isPlanTime = false;
            },
            //标题校验
            titleBlur(){
                var reg = /^[^*\/|:<>?\\"]*$/;
                if(!this.title || !reg.test(this.title)){
                    this.isTitle = true;
                }else{
                    this.isTitle = false;
                }
            },
            //交付件标题校验
            deliverBlur(){
                var reg = /^[^*\/|:<>?\\"]*$/;
                if(!this.deliverName || !reg.test(this.deliverName)){
                    this.isDeliver = true;
                }else{
                    this.isDeliver = false;
                }
            },
            //责任人校验
            ownerBlur(str=this.owner){
                var len = D.jobNumberArr(str).length;
                if(len>20){
                    this.isOwner = true;
                }else{
                    this.isOwner = false;
                }
            },
            changePermissions(){
                let  self  = this;
                if(this.Selected=='1' || this.Selected=='4'){
                    this.isDisabled=true;
                }else if(this.Selected=='2'){
                    this.isDisabled=false;
                    this.getGroupMembers(this.groupsIds);
                }
                else{
                    this.isDisabled=false;
                    this.getGroups();
                }
                this.Permission='';
                this.PermissionIds='';
                 setTimeout(function(){
                    self.$refs.Permission.text=this.Permission;
                },500);
            },
            //浏览权限校验
            PermissionsBlur(str=this.Permission){
                // console.log("this.Permission 000", this.Permission);
                let self =this;
                self.Permission='';
                self.PermissionIds='';
                if (str){
                    str=encodeURI(str);
                    this.$http({
                  //url:'/project/ui/personselector?term='+str,
                  url:'/project/member/veriPermissions.json?projectId='+this.projectId+'&Ids='+str,
                  method:'get',
                  dataType:'text',
                }).then(function(res){
                    res = res.data;
                    if(res.head.flag){
                        var arr= res.body.message;
                        for (var i in arr){
                            var array = arr[i];
                            if (array==[] || array.length==0){
                            }else{
                                self.Permission=array[0].title+";"+self.Permission;
                                self.PermissionIds=array[0].id+";"+self.PermissionIds;
                            }
                        }
                        if (!self.isDisabled) {
                            self.$refs.Permission.text=self.Permission;
                        }
                    }
                }).catch(function(res){
                    D.showMsg();
                });
                }
            },
            getTaskDetail(){
                let self=this;
                self.resetAll();
                if(!this.taskId){
                    return;
                }
                //编辑默认不可选择交付件文档
                self.isSelTempdocName = false;
                D.block();
                this.$http.get("/project/task/getTaskDetail.json?"+(new Date()).getTime(),{
                    params:{'taskId':self.taskId}})
                .then(function(res){
                    D.unblock();
                    res=res.data;
                    if(res.head.flag){
                        // console.log("username:"+D.userName);
                        var node=res.body.deliverableNode;
                        var deliverable=res.body.deliverable;
                        var taskInfo=res.body.taskInfoEx;
                        var deliverableproperty = [];
                        if(res.body.deliverable){
                            deliverableproperty = res.body.deliverable.deliverableProperty;
                        }
                        //非责任者，后台管理员没有权限
                        // if(!taskInfo.ownerList.includes(D.userName) && !self.authorized){
                        //     self.$router.replace('/nopermission');
                        //     return;
                        // }
                        var peimissionL = [];
                        if(taskInfo){
                            self.title=taskInfo.taskName;
                            self.getGroups();
                            if(taskInfo.ownerList && taskInfo.ownerList.length>0){
                                self.owner = taskInfo.ownerList.join(";")+";";
                            }
                            if(deliverableproperty && deliverableproperty.length > 0){

                                if (deliverableproperty[0].attributeValue.substring(0,4) == 'PGRO') {
                                    deliverableproperty.forEach((item)=>{
                                        peimissionL.push(item.attributeValue);
                                    });
                                    self.getGroups();
                                    self.checkedArr = peimissionL;
                                    //self.Permission = peimissionL.join(';');
                                    self.getCheckedNames();
                                    self.groupsIds = deliverableproperty.attributeValue;
                                    self.Selected = "3";
                                    self.isDisabled = false;
                                    self.Permission = res.body.authorizedName.join(";");
                                }else if(deliverableproperty[0].attributeValue == "ALL"){
                                    self.Selected = "1";
                                    self.isDisabled = true;
                                    self.PermissionIds = [];
                                }else if(deliverableproperty[0].attributeValue == "PDIS"){
                                    self.Selected = "4";
                                    self.isDisabled = true;
                                    self.PermissionIds = [];
                                }else{
                                    deliverableproperty.forEach((item)=>{
                                    peimissionL.push(item.attributeValue);
                                    });
                                    self.getGroupMembers(self.groupsIds);
                                    self.checkedArr = peimissionL;
                                    //self.Permission = peimissionL.join(';');
                                    self.getMemerCheckedNames();
                                    self.Selected = "2";
                                    self.isDisabled = false;
                                }
                            }

                            self.status=taskInfo.status;
                            //任务类型
                            setTimeout(()=>{
                                            if (self.owner) {
                                  self.$refs.author.setText(self.owner);
                                            }
                                            if (self.Permission) {
                                                self.$refs.Permission.text=self.Permission;
                                            }
                                self.taskTypeName = taskInfo.taskTypeName;
                                self.$refs.taskType.placeholder = self.taskTypeName;
                                self.$refs.taskType.reset();
                                self.taskTypeId=taskInfo.taskType;
                                //任务模板
                                self.taskTempName=taskInfo.taskTemplateName;
                                self.$refs.taskTemp.placeholder = self.taskTempName;
                                self.$refs.taskTemp.reset();
                                self.taskTempId=taskInfo.taskTemplateId;
                                self.taskDes=taskInfo.description;
                            },500)

                            //文档助手
                            self.docAssistant=taskInfo.docAssistant;
                            self.docName =  taskInfo.docAssistantName;
                            //模板文档
                            self.tempdocId=taskInfo.templateDoc;
                            self.tempdocName=taskInfo.templateDocName;
                            //关键字
                            self.searchWords=taskInfo.searchWords;
                            //相关资料
                            self.relateItems=taskInfo.relateItems || [];
                            //缓存原有的参考说明
                            self.relatedCopyList = [];
                            self.relatedCopyList = taskInfo.relateItems  || [];
                            //解决IE提示和回显冲突问题
                            if(taskInfo.finishTime){
                                self.planTimeName= "";
                            }
                            else{
                                self.planTimeName= "请选择日期";
                            }
                            self.planTime=taskInfo.finishTime;
                            //交付文档Id
                            self.nodeId = taskInfo.nodeId;
                            //交付件主键Id
                            self.dleId=res.body.dleId;
                            //已选的辅助工具
                            //self.assisantTools = taskInfo.assisantToolsList || [];
                            self.getdomainbyAuxiliary(taskInfo.auxiliaryTools);
                            // console.log("this.nodeId", self.nodeId)
                        }
                        if (deliverable) {
                            self.deliverableType = deliverable.deliverableType;
                        }
                        console.log("deliverableType", self.deliverableType)
                        //根据任务乐行初始化动态配置属性
                        self.initUpdateTaskType(taskInfo.taskTypeTerm);
                        //交付件属性
                        if(node){
                            self.isDeliverable = true;
                            if(deliverable){
                                self.deliverName = deliverable.name;
                            } else {
                                self.deliverName=node.name;
                            }
                            var field=node.fieldValues;
                            self.productName=res.body.scopeName;
                            if(field.product){
                                var productIds = field.product.values;
                                if(productIds.length==1){
                                    self.productId = productIds[0];
                                }
                            }
                            if(field.vrVersion &&field.vrVersion.values.length>0){
                                self.vesVrName=res.body.vrverionName;
                                self.$refs.vrVersion.placeholder = self.vesVrName;
                                self.$refs.vrVersion.reset();
                                self.vrId = field.vrVersion.values[0];
                            }
                            if(field.cVersion && field.cVersion.values.length>0){
                                self.vesCName=res.body.cverionName;
                                self.$refs.cVersion.placeholder = self.vesCName;
                                self.$refs.cVersion.reset();
                                self.cId = field.cVersion.values[0];
                            }
                            self.deliverkeywords=node.keywords;
                            self.labels = [];
                            var nodekeywords = [];
                            if (node.keywords) {
                                nodekeywords = node.keywords.split(";");
                                var len = nodekeywords.length;
                                var num = len - 1;
                                nodekeywords.forEach(item=>{
                                    if(item == ''){
                                        nodekeywords.splice(num,1);
                                    }else{
                                        return;
                                    }
                                })
                                self.labels = nodekeywords;
                            }
                            self.deliverDes=node.description;
                        } else {
                            //交付件信息为空，可编辑交付件文档
                            self.isSelTempdocName = true;
                        }
                    }else{
                        D.showMsg("获取任务详情失败，请联系管理员");
                    }
                    // console.log("task:"+res.data);
                }).catch(function(){
                    D.showMsg('创建失败,网络服务异常,请联系管理员!');
                });;;
            },
            initUpdateTaskType(taskType) {
                //初始化任务模板详情
                this.initTaskTempInfo(taskType);

                //初始化交付件属性
                this.initDeliverableConfig(taskType);
            },

            //是否后台管理员
            isAuthorized(){
                let self=this;
                if(D.allRoles){
                    D.allRoles.forEach(function(item){
                        if('DFXRoleType-BEAdmin'==item.roleType){
                            self.authorized=true;
                            return;
                        }
                    });
                }
            },
            cancletemp(){
                let self = this;
                self.checkedArr = [];
                self.groupsArr = [];
                self.owner='';
                if(!self.title){
                  self.owner = D.commonName;
                }
                D.showMsg('确定取消吗？',function(){
                    self.taskDialog=false;
                },true,false);
                //window.location.href="/myspace/project/list";
            },
            keywordsBlur(){
                this.searchWords=this.searchWords.trim();
                if(this.searchWords &&this.searchWords.length>100){
                    this.iskeywords=true;
                }else{
                    this.iskeywords=false;
                }
            },
            taskdesBlur(){
                if(this.taskDes.length>1000){
                    this.isTaskdes=true;
                }else{
                    this.isTaskdes=false;
                }
            },
            selectProduct() {
                this.$refs.product.topTermQuery();
                this.productScopeDialog = true;
            },
            viewDoc(nodeId){
                if(!nodeId){
                    return;
                }
                D.block();
                this.$http.get("/project/document/checkHaveIdp.json",{
                    params:{"nid":nodeId}})
                .then(function(res){
                    D.unblock();
                    res = res.data;
                    if(res.head.flag){
                        if(res.body.flag){
                            window.open("/document/publish/idpview/"+nodeId);
                        }else{
                            window.open("/document/detail/"+nodeId);
                        }
                    }else{
                        D.showMsg("检测idp文档失败，请联系管理员");
                    }
                })
                .catch(function(){
                    D.unblock();
                    D.showMsg();
                });
            },
            fileChange(inputEle){

                let self = this;

                let file = this.$refs.inputer.files;
                //解决IE11上传附件触发filechange事件两次的问题
                // if(file.length == 0 && self.contentInfoList.length == self.toDeletePartNoList.length){
                if(file.length == 0){
                    return;
                }
                //判断文件个数

                let formdata = new FormData();
                //文件插入到formData
                for(var i = 0;i < file.length;i++){
                    // 附件名称长度判断，不能超过80个字符
                    var name = file[i].name;
                    file[i].firstName = file[i].name.split('.')[0];
                    if(file[i].size < 1){
                        D.showMsg("选择的文件内容不能为空，请重新选择");
                        return this.$refs.inputer.value='';
                    }
                    if(file[i].size/1024/1024 > 500){
                        D.showMsg("选择的文件不能大于500M，请重新选择");
                        return this.$refs.inputer.value='';
                    }
                    if(!name.endsWith(".doc") && !name.endsWith(".docx")){
                        D.showMsg("选择的文件只能选择Word文档，请重新选择");
                        return this.$refs.inputer.value='';
                    }

                    formdata.append('file',file[i]);
                };
                this.$refs.inputer.value = '';
                //发送formdata到临时目录，返回一个数组
                D.block();
                this.$http.post('/project/upload/tempAttachments.json',formdata,{
                    headers:{
                        "Content-Type": "multipart/form-data"
                    }
                }).then(res => {
                    D.unblock();
                    res = res.data;
                    if(res.head && res.head.flag){
                        console.log('upload',res);
                        self.fileItems = res.body.message;
                        if(self.fileItems){
                            var fileName = self.fileItems[0].FILE_NAME;
                            //模版文档
                            self.tempdocName = fileName;
                            //交付件名称
                            self.deliverName = fileName.substring(0,fileName.lastIndexOf("."));
                            self.tempdocId = "";
                        }
                    } else {
                         D.showMsg("添加文件失败");
                    }
                }).catch(res => {
                  D.showMsg("服务器错误，请重新上传.")})
            },
        },
        watch:{
            //文档选择
            'id'(newVal,oldVal){
                let self = this;
                if(newVal){
                    this.docList.forEach(function(item){
                        if(item.id === newVal){
                            self.docObj = item
                        }

                    })
                }
            },
            taskDialog(){
                if(this.taskDialog && !this.taskId){
                    this.isDefaultdocTask = true;
                    this.initData();
                    this.getGroups();
                }
            }
        },
        props:['indexItem','pId']
    }
</script>
<!--taskDialogEnd-->
<!--URLEncodedUtils-->
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

public class URLEncodedUtils
{
    public static String decode(String content, String encoder)
    {
        String enc = StringUtils.isBlank(encoder) ? "utf-8" : encoder;
        try
        {
            String fixedContent = content.replaceAll("\\+", "%2B");
            return URLDecoder.decode(fixedContent, enc);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
    
    public static String encode(String content, String encoder)
    {
        if (StringUtils.isBlank(content))
        {
            return content;
        }
        String enc = StringUtils.isBlank(encoder) ? "utf-8" : encoder;
        //content = content.replaceAll(" ", "%20");
        try
        {
            String result = URLEncoder.encode(content, enc);
            return result.replaceAll("\\+", "%20");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalArgumentException(e);
        }
    }}
    
