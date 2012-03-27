import exportapp.Book
class BootStrap {

    def init = { servletContext ->        
        if(!Book.count()){
            createData()
        }
    }
    
    private void createData(){
        def b = new Book(title: 'Spiders', author:'Jefferson Almeida').save()
    }
    
    def destroy = {
    }
}
