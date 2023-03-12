new Vue({
    el: '#app',
    data: () => {
        return {
            submitUploadBtnState: false,
            tableData: [],
            percentage: 0
        }
    },
    methods: {
        handleRemove(file) {
            document.getElementById("uploadTip").innerText = "请选择上传文件"
            this.$data.tableData = []
            console.log('删除选择文件', file);
        },
        handlePreview(file) {
            console.log('预览选择文件', file);
        },
        beforeRemove(file) {
            return this.$confirm(`确定移除 ${file.name}？`);
        },
        async submitUpload() {
            this.$refs.upload.submit();
        },
        async uploadFile(value) {
            await uploadFile(value.file, (err, data) => {
                this.$data.percentage = Math.floor(data['segmentIndex'] / data['segmentTotal'] * 100);
                if (this.$data.percentage < 100) {
                    return;
                }
                const temp = {
                    fileName: data['fileName'],
                    fileUrl: data['fileUrl'],
                }
                this.$refs.upload.clearFiles();
                this.$data.tableData.push(temp);
                this.$notify({
                    title: '成功',
                    message: '上传成功',
                    type: 'success'
                });
            });
        },
        copyFileUrl(row) {
            navigator.clipboard.writeText(row['fileUrl']).then(() => {
                this.$message({
                    message: '复制成功',
                    type: 'success'
                });
            });
        }
    },
});