<?php

namespace App\Controller;

use App\Entity\Reunion;
use App\Form\ReunionType;
use App\Repository\ReunionRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/reunions')]
class ReunionController extends AbstractController
{
    #[Route('', name: 'app_reunion_index')]
    public function index(ReunionRepository $repository): Response
    {
        return $this->render('reunion/index.html.twig', ['items' => $repository->findAll()]);
    }

    #[Route('/new', name: 'app_reunion_new')]
    public function create(Request $request, EntityManagerInterface $em): Response
    {
        $item = new Reunion();
        $form = $this->createForm(ReunionType::class, $item);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $em->persist($item);
            $em->flush();
            return $this->redirectToRoute('app_reunion_index');
        }

        return $this->render('reunion/form.html.twig', ['form' => $form]);
    }
}
