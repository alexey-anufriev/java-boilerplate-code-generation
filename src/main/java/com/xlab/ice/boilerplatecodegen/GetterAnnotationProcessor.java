package com.xlab.ice.boilerplatecodegen;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes(value = GetterAnnotationProcessor.ANNOTATION)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class GetterAnnotationProcessor extends AbstractProcessor {

    public static final String ANNOTATION = "com.xlab.ice.boilerplatecodegen.Getter";
    private JavacProcessingEnvironment javacProcessingEnvironment;
    private TreeMaker treeMaker;
    private Messager messager;
    private Context context;
    private Trees trees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        javacProcessingEnvironment = (JavacProcessingEnvironment) processingEnv;
        context = javacProcessingEnvironment.getContext();
        treeMaker = TreeMaker.instance(context);
        messager = processingEnv.getMessager();
        trees = Trees.instance(processingEnv);
        log("Init done.");
    }

    private void log(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        log("Processing started.");
        if(annotations != null && !annotations.isEmpty()) {
            log("Annotations found.");
            Elements elements = javacProcessingEnvironment.getElementUtils();
            TypeElement typeElement = elements.getTypeElement(ANNOTATION);
            if(typeElement != null) {
                log("Type " + typeElement.getSimpleName());
                Set<? extends Element> fields =
                        roundEnv.getElementsAnnotatedWith(typeElement);
                log("Fields " + fields);
                JavacElements utils = javacProcessingEnvironment.getElementUtils();
                for(Element field : fields) {
                    log("Field " + field.getSimpleName());
                    Getter getter = field.getAnnotation(Getter.class);
                    if(getter != null) {
                        JCTree node = utils.getTree(field);
                        if(node instanceof JCTree.JCVariableDecl) {
                            JCTree.JCStatement returnStatement = treeMaker.Return(
                                    treeMaker.Ident(((JCTree.JCVariableDecl) node).getName())
                            );
                            JCTree.JCBlock methodBody = treeMaker.Block(0, List.of(returnStatement));
                            Name methodName = createGetterMethodName(context, ((JCTree.JCVariableDecl) node).getName());
                            JCTree.JCExpression returnType = ((JCTree.JCVariableDecl) node).vartype;
                            List<JCTree.JCTypeParameter> generics = List.nil();
                            List<JCTree.JCVariableDecl> parameters = List.nil();
                            List<JCTree.JCExpression> exceptions = List.nil();
                            JCTree.JCExpression annotation = null;
                            JCTree.JCMethodDecl getterMethod = treeMaker.MethodDef(
                                    treeMaker.Modifiers(Flags.PUBLIC, List.<JCTree.JCAnnotation>nil()),
                                    methodName, returnType, generics, parameters, exceptions, methodBody,
                                    annotation
                            );
                            log("Method name " + getterMethod.getName());
                            log("Method body " + getterMethod.getBody());
                            log("Method access " + getterMethod.getModifiers());
                            log("Method signature " + getterMethod.getReturnType());
                            TypeElement type = (TypeElement) field.getEnclosingElement();
                            ClassTree classTree = trees.getTree(type);
                            JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) classTree;
                            log("Class " + classDecl.getSimpleName());
                            classDecl.defs = classDecl.defs.append(getterMethod);
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static Name createGetterMethodName(Context c, Name fieldName) {
        String fieldNameStr = fieldName.toString();
        String methodNameStr = Character.toUpperCase(fieldNameStr.charAt(0))
                + fieldNameStr.substring(1);
        return (new Names(c)).fromString("get" + methodNameStr);
    }
}
