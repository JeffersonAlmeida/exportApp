package exportapp

import org.springframework.dao.DataIntegrityViolationException
import org.codehaus.groovy.grails.commons.ConfigurationHolder

import exportapp.Book

class BookController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    
     // Export service provided by Export plugin	
    def exportService
    def ConfigurationHolder
    
    def generateFile(){
        
        System.out.println("parametroMax : " + params.max + "\nparametroFormat: " + params.format);
        
        if(!params.max){ 
            params.max = 10
        }
        if(params?.format && params.format != "html"){
            //response.contentType = ConfigurationHolder.config.grails.mime.types[params.format]
            response.contentType = ['application/json','text/json'] // definição estatica. sempre será definido como JSON
            response.setHeader("Content-disposition", "attachment; filename=books.${params.extension}")
            List fields = ["author", "title"]
            Map labels = ["author": "Author", "title": "Title"]

            // Formatter closure
            def upperCase = { domain, value ->
                    return value.toUpperCase()
            }

            Map formatters = [author: upperCase]		
            Map parameters = [title: "Cool books", "column.widths": [0.2, 0.3, 0.5]]

            exportService.export(params.format, response.outputStream, Book.list(params), fields, labels, formatters, parameters)
        }
        
        def bookInstance = Book.list( params ) 
        
        render template: 'pagina', collection: bookInstance, var: 'bookInstance'
    }

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [bookInstanceList: Book.list(params), bookInstanceTotal: Book.count()]
    }

    def create() {
        [bookInstance: new Book(params)]
    }

    def save() {
        def bookInstance = new Book(params)
        if (!bookInstance.save(flush: true)) {
            render(view: "create", model: [bookInstance: bookInstance])
            return
        }

		flash.message = message(code: 'default.created.message', args: [message(code: 'book.label', default: 'Book'), bookInstance.id])
        redirect(action: "show", id: bookInstance.id)
    }

    def show() {
        def bookInstance = Book.get(params.id)
        if (!bookInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'book.label', default: 'Book'), params.id])
            redirect(action: "list")
            return
        }

        [bookInstance: bookInstance]
    }

    def edit() {
        def bookInstance = Book.get(params.id)
        if (!bookInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'book.label', default: 'Book'), params.id])
            redirect(action: "list")
            return
        }

        [bookInstance: bookInstance]
    }

    def update() {
        def bookInstance = Book.get(params.id)
        if (!bookInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'book.label', default: 'Book'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (bookInstance.version > version) {
                bookInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'book.label', default: 'Book')] as Object[],
                          "Another user has updated this Book while you were editing")
                render(view: "edit", model: [bookInstance: bookInstance])
                return
            }
        }

        bookInstance.properties = params

        if (!bookInstance.save(flush: true)) {
            render(view: "edit", model: [bookInstance: bookInstance])
            return
        }

		flash.message = message(code: 'default.updated.message', args: [message(code: 'book.label', default: 'Book'), bookInstance.id])
        redirect(action: "show", id: bookInstance.id)
    }

    def delete() {
        def bookInstance = Book.get(params.id)
        if (!bookInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'book.label', default: 'Book'), params.id])
            redirect(action: "list")
            return
        }

        try {
            bookInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'book.label', default: 'Book'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'book.label', default: 'Book'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
}
